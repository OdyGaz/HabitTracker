package com.odygaz.habittracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private val onDeleteClick: (Habit) -> Unit,
    private val onCircleClick: (habitId: Int, day: Int, isCompleted: Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private var habits = emptyList<Habit>()
    private var records = emptyList<HabitRecord>()

    // Συνάρτηση για να ανανεώνουμε τα δεδομένα της λίστας
    fun setData(newHabits: List<Habit>, newRecords: List<HabitRecord>) {
        this.habits = newHabits
        this.records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.textHabitName.text = habit.name

        // Κλικ στο "x" για διαγραφή
        holder.btnDeleteHabit.setOnClickListener {
            onDeleteClick(habit)
        }

        // Καθαρισμός του container για να μην διπλασιάζονται οι κύκλοι κατά το scroll
        holder.circlesContainer.removeAllViews()

        // Φιλτράρισμα καταγραφών για τη συγκεκριμένη συνήθεια
        val habitRecords = records.filter { it.habitId == habit.id }

        // Υπολογισμός μεγέθους κύκλου σε pixel με βάση την πυκνότητα (density) της οθόνης
        val density = holder.itemView.context.resources.displayMetrics.density
        val circleSizePx = (16 * density).toInt() // 16dp μέγεθος κύκλου

        // Δημιουργούμε δυναμικά 31 κύκλους
        for (day in 1..31) {
            val circleView = View(holder.itemView.context)

            // Ορίζουμε ίσο βάρος (weight = 1.0f) ώστε οι κύκλοι να μοιραστούν τέλεια στην οθόνη
            val layoutParams = LinearLayout.LayoutParams(0, circleSizePx, 1.0f)
            layoutParams.setMargins(4, 4, 4, 4) // Μικρά περιθώρια (margins) γύρω από κάθε κύκλο
            circleView.layoutParams = layoutParams

            // Έλεγχος αν η συγκεκριμένη μέρα είναι ολοκληρωμένη
            val isCompleted = habitRecords.any { it.day == day && it.isCompleted }
            circleView.setBackgroundResource(
                if (isCompleted) R.drawable.circle_filled else R.drawable.circle_empty
            )

            // Κλικ πάνω στον κύκλο (Check / Uncheck)
            circleView.setOnClickListener {
                onCircleClick(habit.id, day, !isCompleted)
            }

            holder.circlesContainer.addView(circleView)
        }
    }

    override fun getItemCount(): Int = habits.size

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textHabitName: TextView = itemView.findViewById(R.id.textHabitName)
        val btnDeleteHabit: TextView = itemView.findViewById(R.id.btnDeleteHabit)
        val circlesContainer: LinearLayout = itemView.findViewById(R.id.circlesContainer)
    }
}