package com.glebmorgan.computerquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class QuestionFragment extends Fragment {

    private static final String QUESTION_ID = "question_id";

    private Question mQuestion;
    private boolean[] mAnswers;
    private boolean[] mCheckedAnswers;

    private Button mFirstAnswerButton;
    private Button mSecondAnswerButton;
    private Button mThirdAnswerButton;
    private Button mFourthAnswerButton;

    private TextView mQuestionText;

    public QuestionFragment() {
        // Required empty public constructor
    }

    public static QuestionFragment newInstance(int id) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putInt(QUESTION_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnswers = ((App) getActivity().getApplication()).getAnswers();
        mCheckedAnswers = ((App) getActivity().getApplication()).getCheckedAnswers();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        mFirstAnswerButton = view.findViewById(R.id.first_answer);
        mFirstAnswerButton.setOnClickListener(this::onClick);

        mSecondAnswerButton = view.findViewById(R.id.second_answer);
        mSecondAnswerButton.setOnClickListener(this::onClick);

        mThirdAnswerButton = view.findViewById(R.id.third_answer);
        mThirdAnswerButton.setOnClickListener(this::onClick);

        mFourthAnswerButton = view.findViewById(R.id.fourth_answer);
        mFourthAnswerButton.setOnClickListener(this::onClick);

        mQuestionText = view.findViewById(R.id.question_text);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("questions");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String questionId = String.valueOf(QuestionFragment.this.getArguments()
                        .getInt(QUESTION_ID, 0));

                mQuestion = dataSnapshot.child(questionId).getValue(Question.class);

                mFirstAnswerButton.setText(mQuestion.getFirstAnswer());
                mSecondAnswerButton.setText(mQuestion.getSecondAnswer());
                mThirdAnswerButton.setText(mQuestion.getThirdAnswer());
                mFourthAnswerButton.setText(mQuestion.getFourthAnswer());

                mQuestionText.setText(mQuestion.getQuestion());
                if (mCheckedAnswers[mQuestion.getId()]) setButtonsUnclickable();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        return view;
    }

    public void onClick(View view) {
        int answer = mQuestion.getNumOfRightAnswer();
        Button button = (Button) view;
        Log.d("asd", "click");
        if (answer == getButtonNum(view)) {
            button.setBackgroundColor(getResources().getColor(R.color.green));
            mAnswers[mQuestion.getId()] = true;
        } else {
            button.setBackgroundColor(getResources().getColor(R.color.red));
            mAnswers[mQuestion.getId()] = false;
        }
        mCheckedAnswers[mQuestion.getId()] = true;
        int count = 0;
        for (boolean checkedAnswer : mCheckedAnswers) {
            if (checkedAnswer) count++;
        }
        if (count == mAnswers.length) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("results");
            Result result = new Result();
            result.setId(myRef.push().getKey());
            result.setEmail(((App) getActivity().getApplication()).getUser().getEmail());
            int score = 0;
            for (boolean rightAnswer : mAnswers) {
                if (rightAnswer) score++;
            }
            result.setScore(score);
            myRef.child(result.getId()).setValue(result);
            Intent intent = new Intent(getActivity(), ResultActivity.class);
            startActivity(intent);
        }
        setButtonsUnclickable();
    }

    private void setButtonsUnclickable() {
        mFirstAnswerButton.setClickable(false);
        mSecondAnswerButton.setClickable(false);
        mThirdAnswerButton.setClickable(false);
        mFourthAnswerButton.setClickable(false);
    }

    private int getButtonNum(View view) {
        switch (view.getId()) {
            case R.id.first_answer:
                return 1;
            case R.id.second_answer:
                return 2;
            case R.id.third_answer:
                return 3;
            case R.id.fourth_answer:
                return 4;
            default:
                return 0;
        }
    }
}