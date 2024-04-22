package com.example.gradebook;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

@SuppressLint({"SetTextI18n", "DefaultLocale"})
public class MainActivity extends AppCompatActivity {
    ArrayList<Student> students = new ArrayList<>();
    ImageButton add_student_btn, sort_btn, statistics_btn, menu_btn;
    EditText search;
    RecyclerView list;
    StudentAdapter adapter;
    private int currentSortMode = R.id.item1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        add_student_btn = findViewById(R.id.add_student_btn);
        sort_btn = findViewById(R.id.sort_btn);
        statistics_btn = findViewById(R.id.statistics_btn);
        menu_btn = findViewById(R.id.menu_btn);

        search = findViewById(R.id.searchView);
        search.clearFocus();

        list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(students);
        list.setAdapter(adapter);

        menu_btn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, menu_btn);
            popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId()==R.id.clear) {
                    new AlertDialog.Builder(this)
                            .setTitle("Confirm clear")
                            .setMessage("Are you sure you want to clear the students table? Cause if you clear table, all students will be removed!")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Toast.makeText(MainActivity.this, "All students removed", Toast.LENGTH_SHORT).show();
                                students.clear();
                                search.setText("");
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return true;
                }
                if (item.getItemId()==R.id.refresh) {
                    Toast.makeText(MainActivity.this, "Students table updated", Toast.LENGTH_SHORT).show();
                    search.setText("");
                    return true;
                }
                if (item.getItemId()==R.id.exit) {
                    onBackPressed();
                    return true;
                }
                if (item.getItemId()==R.id.download) {
                    Toast.makeText(MainActivity.this, "Students table downloaded", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            });
            popupMenu.show();
        });

        add_student_btn.setOnClickListener(v -> {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.add_student, null);

            EditText addId = dialogView.findViewById(R.id.add_id);
            EditText addName = dialogView.findViewById(R.id.add_name);
            EditText addScore = dialogView.findViewById(R.id.add_score);
            Button buttonOk = dialogView.findViewById(R.id.ok_btn_add);
            Button buttonCancel = dialogView.findViewById(R.id.cancel_btn_add);

            TextView add_id_error = dialogView.findViewById(R.id.add_id_error);
            TextView add_name_error = dialogView.findViewById(R.id.add_name_error);
            TextView add_score_error = dialogView.findViewById(R.id.add_score_error);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(dialogView);


            AlertDialog dialog = builder.create();
            dialog.show();


            buttonOk.setOnClickListener(v1 -> {
                String idText = addId.getText().toString();
                String nameText = addName.getText().toString();
                String scoreText = addScore.getText().toString();

                if (idText.isEmpty()) {
                    add_id_error.setText("• ID field is empty.");
                    return;
                } else if (idText.length()!=5) {
                    add_id_error.setText("• ID must consist a 5-digit number.");
                    return;
                } else if (isDuplicateId(idText)) {
                    add_id_error.setText("• ID already exists.");
                    return;
                } else {
                    add_id_error.setText("");
                }

                if (nameText.isEmpty()) {
                    add_name_error.setText("• Name field is empty.");
                    return;
                } else if (nameText.length()>18) {
                    add_name_error.setText("• Name is too long.");
                    return;
                } else if (!nameText.matches("[a-zA-Z\\s']+")) {
                    add_name_error.setText("• Name must consist of letters only.");
                    return;
                } else {
                    add_name_error.setText("");
                }

                if (scoreText.isEmpty()) {
                    add_score_error.setText("• Score field is empty");
                    return;
                } else if (Integer.parseInt(scoreText) > 100) {
                    add_score_error.setText("• Score must be between 0 and 100.");
                    return;
                }else {
                    add_score_error.setText("");
                }


                students.add(new Student(idText, nameText, Integer.parseInt(scoreText)));

                addId.setText("");
                addName.setText("");
                addScore.setText("");

                sortCurrent();
                search.setText("");

                Toast.makeText(MainActivity.this, "Student added", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            buttonCancel.setOnClickListener(v12 -> dialog.dismiss());
        });

        statistics_btn.setOnClickListener(v -> {
            if (students.size()>0) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.statistics, null);

                int totalStudents = students.size();
                double averageScore=0;
                int highestScore = 0;
                int lowestScore = 100;

                for (Student s:students) {
                    int score = s.getScore();
                    averageScore+=score;
                    highestScore = Math.max(highestScore, score);
                    lowestScore = Math.min(lowestScore, score);
                }

                averageScore/=totalStudents;
                char finalGrade = Student.calculate(averageScore);

                TextView totalStudentsTextView = dialogView.findViewById(R.id.statistics_total);
                totalStudentsTextView.setText("Total Students: " + totalStudents);

                TextView averageScoreTextView = dialogView.findViewById(R.id.statistics_average);
                averageScoreTextView.setText("Average Score: " + String.format("%.2f", averageScore));

                TextView highestScoreTextView = dialogView.findViewById(R.id.statistics_highest);
                highestScoreTextView.setText("Highest Score: " + highestScore);

                TextView lowestScoreTextView = dialogView.findViewById(R.id.statistics_lowest);
                lowestScoreTextView.setText("Lowest Score: " + lowestScore);

                TextView finalGradeTextView = dialogView.findViewById(R.id.statistics_grade);
                finalGradeTextView.setText("Final Grade: " + finalGrade);


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(dialogView);

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(MainActivity.this, "No students added yet", Toast.LENGTH_SHORT).show();
            }
        });

        sort_btn.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, sort_btn);
            popupMenu.getMenuInflater().inflate(R.menu.sort_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                currentSortMode = item.getItemId();
                if (currentSortMode==R.id.item1) {
                    students.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
                    Toast.makeText(MainActivity.this, "Sorted by A-Z", Toast.LENGTH_SHORT).show();
                    search.setText("");
                    return true;
                }
                if (currentSortMode==R.id.item2) {
                    students.sort((s1, s2) -> s2.getName().compareToIgnoreCase(s1.getName()));
                    Toast.makeText(MainActivity.this, "Sorted by Z-A", Toast.LENGTH_SHORT).show();
                    search.setText("");
                    return true;
                }
                if (currentSortMode==R.id.item3) {
                    students.sort(Comparator.comparingInt(Student::getScore));
                    Toast.makeText(MainActivity.this, "Sorted by lowest-highest", Toast.LENGTH_SHORT).show();
                    search.setText("");
                    return true;
                }
                if (currentSortMode==R.id.item4) {
                    students.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));
                    Toast.makeText(MainActivity.this, "Sorted by highest-lowest", Toast.LENGTH_SHORT).show();
                    search.setText("");
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                ArrayList<Student> filteredStudents = new ArrayList<>();
                for (Student student : students) {
                    if (student.getName().toLowerCase().contains(text.toLowerCase())) {
                        filteredStudents.add(student);
                    }
                }
                adapter.setFilteredList(filteredStudents);
            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.LEFT) {
                    Student removedStudent = adapter.students.get(position);
                    students.remove(removedStudent);
                    sortCurrent();
                    search.setText("");
                    Snackbar.make(list, "Student removed", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                if (removedStudent != null) {
                                    students.add(removedStudent);
                                    sortCurrent();
                                    search.setText("");
                                }
                            }).show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    manageStudent(position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (dX > 0) {
                        // Swiping right
                        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                                .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.blue))
                                .addSwipeRightActionIcon(R.drawable.edit_icon)
                                .addSwipeRightLabel("Edit")
                                .setSwipeRightLabelColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                                .create()
                                .decorate();
                    } else {
                        // Swiping left
                        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                                .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red))
                                .addSwipeLeftActionIcon(R.drawable.trash_icon)
                                .addSwipeLeftLabel("Delete")
                                .setSwipeLeftLabelColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                                .create()
                                .decorate();
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(list);
    }
    public boolean isDuplicateId(String id) {
        for (Student student : students) {
            if (student.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
    public void sortCurrent() {
        if (currentSortMode==R.id.item1) {
            students.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));
        }  else if (currentSortMode==R.id.item2) {
            students.sort((s1, s2) -> s2.getName().compareToIgnoreCase(s1.getName()));
        }  else if (currentSortMode==R.id.item3) {
            students.sort(Comparator.comparingInt(Student::getScore));
        }  else if (currentSortMode==R.id.item4) {
            students.sort((s1, s2) -> Integer.compare(s2.getScore(), s1.getScore()));
        }
    }

    public void manageStudent(int position) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.manage_student, null);

        EditText editId = dialogView.findViewById(R.id.manage_id);
        EditText editName = dialogView.findViewById(R.id.manage_name);
        EditText editScore = dialogView.findViewById(R.id.manage_score);
        Button buttonOk = dialogView.findViewById(R.id.ok_btn_manage);
        Button buttonDelete = dialogView.findViewById(R.id.delete_btn_manage);

        TextView manage_id_error = dialogView.findViewById(R.id.manage_id_error);
        TextView manage_name_error = dialogView.findViewById(R.id.manage_name_error);
        TextView manage_score_error = dialogView.findViewById(R.id.manage_score_error);

        Student selectedStudent = adapter.students.get(position);
        editId.setText(selectedStudent.getId());
        editName.setText(selectedStudent.getName());
        editScore.setText(String.valueOf(selectedStudent.getScore()));

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonOk.setOnClickListener(v -> {
            String idText = editId.getText().toString();
            String nameText = editName.getText().toString();
            String scoreText = editScore.getText().toString();

            if (idText.isEmpty()) {
                manage_id_error.setText("• ID field is empty.");
                return;
            } else if (idText.length()!=5) {
                manage_id_error.setText("• ID must consist a 5-digit number.");
                return;
            } else if (isDuplicateId(idText) && !idText.equals(selectedStudent.getId())) {
                manage_id_error.setText("• ID already exists.");
                return;
            } else {
                manage_id_error.setText("");
            }


            if (nameText.isEmpty()) {
                manage_name_error.setText("• Name field is empty.");
                return;
            } else if (nameText.length()>18) {
                manage_name_error.setText("• Name is too long.");
                return;
            } else if (!nameText.matches("[a-zA-Z\\s']+")) {
                manage_name_error.setText("• Name must consist of letters only.");
                return;
            } else {
                manage_name_error.setText("");
            }

            if (scoreText.isEmpty()) {
                manage_score_error.setText("• Score field is empty");
                return;
            } else if (Integer.parseInt(scoreText) > 100) {
                manage_score_error.setText("• Score must be between 0 and 100.");
                return;
            }else {
                manage_score_error.setText("");
            }


            selectedStudent.setId(idText);
            selectedStudent.setName(nameText);
            selectedStudent.setScore(Integer.parseInt(scoreText));

            sortCurrent();
            search.setText("");
            Toast.makeText(MainActivity.this, "Student updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        buttonDelete.setOnClickListener(v -> {
            students.remove(selectedStudent);
            sortCurrent();
            search.setText("");
            Toast.makeText(MainActivity.this, "Student removed", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Are you sure you want to exit the application? Cause if you exit Gradebook, all students will be removed!")
                .setPositiveButton("Yes", (dialog, which) -> MainActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }
}

class Student {
    private String id;
    private String name;
    private int score;

    public Student(String id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public static char calculate(double score) {
        char finalGrade = 0;
        if (score >= 80) {
            finalGrade = 'A';
        } else if (score >= 60) {
            finalGrade = 'B';
        } else if (score >= 40) {
            finalGrade = 'C';
        } else if (score >= 20) {
            finalGrade = 'D';
        } else if (score >= 0) {
            finalGrade = 'F';
        }
        return finalGrade;
    }
}

class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    ArrayList<Student> students;

    public StudentAdapter(ArrayList<Student> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item_layout, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void setFilteredList(ArrayList<Student> filteredStudents) {
        students = filteredStudents;
        notifyDataSetChanged();
    }


    public static class StudentViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private final TextView textView;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.student_item);
            itemView.setOnLongClickListener(this);
        }

        public void bind(Student student) {
            char[] text = new char[41];
            Arrays.fill(text, ' ');

            String c1 = Integer.toString(getAdapterPosition() + 1);
            String c2 = student.getId();
            String c3 = student.getName();
            String c4 = Integer.toString(student.getScore());

            for (int i = 0; i < c1.length(); i++) text[i] = c1.charAt(i);
            for (int i = 0; i < c2.length(); i++) text[i + 3] = c2.charAt(i);
            for (int i = 0; i < c3.length(); i++) text[i + 9] = c3.charAt(i);
            for (int i = 0; i < c4.length(); i++) text[i + 28] = c4.charAt(i);
            text[35] = Student.calculate(student.getScore());

            String res = new String(text);
            textView.setText(res);
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                ((MainActivity) v.getContext()).manageStudent(position);
                return true;
            }
            return false;
        }
    }
}