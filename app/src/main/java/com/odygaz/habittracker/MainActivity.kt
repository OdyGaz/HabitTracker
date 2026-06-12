package com.odygaz.habittracker // Προσοχή: Κράτα το δικό σου package name!

import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: HabitViewModel
    private lateinit var adapter: HabitAdapter

    // Λίστα με όλα τα TextViews των μηνών για να κάνουμε εύκολα highlight
    private val monthTextViews by lazy {
        listOf(
            findViewById<TextView>(R.id.monthJan),
            findViewById<TextView>(R.id.monthFeb),
            findViewById<TextView>(R.id.monthMar),
            findViewById<TextView>(R.id.monthApr),
            findViewById<TextView>(R.id.monthMay),
            findViewById<TextView>(R.id.monthJun),
            findViewById<TextView>(R.id.monthJul),
            findViewById<TextView>(R.id.monthAug),
            findViewById<TextView>(R.id.monthSep),
            findViewById<TextView>(R.id.monthOct),
            findViewById<TextView>(R.id.monthNov),
            findViewById<TextView>(R.id.monthDec)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Αρχικοποίηση Βάσης, Repository και ViewModel
        val database = AppDatabase.getDatabase(this)
        val repository = HabitRepository(database.habitDao())
        val factory = HabitViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HabitViewModel::class.java]

        // 2. Στήσιμο των κόκκινων αριθμών ημερών (1-31) στο πάνω μέρος
        setupDaysHeader()

        // 3. Στήσιμο του RecyclerView (Λίστα Συνήθειων)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewHabits)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HabitAdapter(
            onDeleteClick = { habit -> showDeleteConfirmationDialog(habit) },
            onCircleClick = { habitId, day, isCompleted ->
                viewModel.toggleRecord(habitId, day, isCompleted)
            }
        )
        recyclerView.adapter = adapter

        // 4. Σύνδεση των Μηνών με Click Listeners
        monthTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                viewModel.selectMonth(index + 1) // Οι μήνες στη βάση είναι 1-12
            }
        }

        // 5. Σύνδεση των κουμπιών αλλαγής Έτους
        findViewById<TextView>(R.id.btnPrevYear).setOnClickListener {
            viewModel.selectYear(viewModel.currentYear.value - 1)
        }
        findViewById<TextView>(R.id.btnNextYear).setOnClickListener {
            viewModel.selectYear(viewModel.currentYear.value + 1)
        }

        // 6. Κουμπί Προσθήκης Συνήθειας (+ Add Habit)
        findViewById<TextView>(R.id.btnAddHabit).setOnClickListener {
            showAddHabitDialog()
        }

        // 7. Κουμπί Εξαγωγής σε PC (Export to PC)
        findViewById<TextView>(R.id.btnExport).setOnClickListener {
            exportDataToCSV()
        }

        // 8. Παρακολούθηση (Observation) των δεδομένων της βάσης
        observeData()
    }

    // Δημιουργεί αυτόματα τους αριθμούς 1-31 πάνω από τους κύκλους
    private fun setupDaysHeader() {
        val container = findViewById<LinearLayout>(R.id.daysHeaderContainer)
        container.removeAllViews()

        val density = resources.displayMetrics.density

        // Αριστερό κενό (120dp) για να ευθυγραμμιστεί με το όνομα της συνήθειας
        val spacer = View(this)
        spacer.layoutParams = LinearLayout.LayoutParams((120 * density).toInt(), 1)
        container.addView(spacer)

        // Δημιουργία των 31 αριθμών
        for (i in 1..31) {
            val textView = TextView(this)
            val layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
            layoutParams.setMargins(4, 0, 4, 0)
            textView.layoutParams = layoutParams
            textView.text = i.toString()
            textView.gravity = Gravity.CENTER
            textView.setTextColor(ContextCompat.getColor(this, R.color.header_red))
            textView.typeface = ResourcesCompat.getFont(this, R.font.inkfree)
            textView.textSize = 10f
            container.addView(textView)
        }
    }

    // Παρακολουθεί τις αλλαγές στη βάση και ενημερώνει την οθόνη
    private fun observeData() {
        // Ανανέωση λίστας όταν αλλάζουν οι συνήθειες ή οι κύκλοι
        lifecycleScope.launch {
            combine(viewModel.allHabits, viewModel.recordsForCurrentMonth) { habits, records ->
                Pair(habits, records)
            }.collect { (habits, records) ->
                adapter.setData(habits, records)
            }
        }

        // Ανανέωση του κειμένου του έτους
        lifecycleScope.launch {
            viewModel.currentYear.collect { year ->
                findViewById<TextView>(R.id.textYear).text = year.toString()
            }
        }

        // Highlight του επιλεγμένου μήνα
        lifecycleScope.launch {
            viewModel.currentMonth.collect { month ->
                updateMonthHighlight(month)
            }
        }
    }

    // Κάνει highlight (κόκκινο & υπογραμμισμένο) τον επιλεγμένο μήνα
    private fun updateMonthHighlight(selectedMonth: Int) {
        monthTextViews.forEachIndexed { index, textView ->
            if (index == selectedMonth - 1) {
                textView.setTextColor(ContextCompat.getColor(this, R.color.header_red))
                textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                textView.setTextColor(ContextCompat.getColor(this, R.color.month_blue))
                textView.paintFlags = textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }
    }

    // Παράθυρο εισαγωγής νέας συνήθειας
    private fun showAddHabitDialog() {
        val input = EditText(this)
        input.setSingleLine()

        AlertDialog.Builder(this)
            .setTitle("New Habit")
            .setMessage("What habit do you want to track?")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    viewModel.insertHabit(text)
                } else {
                    Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Παράθυρο επιβεβαίωσης διαγραφής
    private fun showDeleteConfirmationDialog(habit: Habit) {
        AlertDialog.Builder(this)
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}' and all its history?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteHabit(habit.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Εξαγωγή δεδομένων σε μορφή CSV (συμβατό με Excel)
    private fun exportDataToCSV() {
        lifecycleScope.launch {
            val habitsList = viewModel.allHabits.first()
            val recordsList = viewModel.recordsForCurrentMonth.first()

            if (habitsList.isEmpty()) {
                Toast.makeText(this@MainActivity, "No habits to export!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val csvString = StringBuilder()

            // Δημιουργία της πρώτης γραμμής (Header): Habit Name, Day 1, Day 2...
            csvString.append("Habit Name")
            for (i in 1..31) {
                csvString.append(",Day $i")
            }
            csvString.append("\n")

            // Συμπλήρωση των δεδομένων
            for (habit in habitsList) {
                csvString.append(habit.name)
                val habitRecords = recordsList.filter { it.habitId == habit.id }
                for (day in 1..31) {
                    val isDone = habitRecords.any { it.day == day && it.isCompleted }
                    csvString.append(if (isDone) ",1" else ",0")
                }
                csvString.append("\n")
            }

            // Άνοιγμα του μενού κοινής χρήσης (Share) για αποστολή στο PC
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Habit Tracker Export (${viewModel.currentMonth.value}/${viewModel.currentYear.value})")
                putExtra(Intent.EXTRA_TEXT, csvString.toString())
            }
            startActivity(Intent.createChooser(intent, "Export Habit Tracker Data"))
        }
    }
}