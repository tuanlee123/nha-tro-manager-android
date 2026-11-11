package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvExpression;
    private TextView tvResult;

    private String currentNumber = "0";
    private Double firstOperand = null;
    private String operator = null;
    private boolean isNewOperation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);
    }

    private void setupListeners() {
        // Number buttons
        int[] numberButtonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int id : numberButtonIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> onNumberClick(btn.getText().toString()));
        }

        // Dot button
        findViewById(R.id.btnDot).setOnClickListener(v -> onDotClick());

        // Operator buttons
        findViewById(R.id.btnAdd).setOnClickListener(v -> onOperatorClick("+"));
        findViewById(R.id.btnSubtract).setOnClickListener(v -> onOperatorClick("−"));
        findViewById(R.id.btnMultiply).setOnClickListener(v -> onOperatorClick("×"));
        findViewById(R.id.btnDivide).setOnClickListener(v -> onOperatorClick("÷"));

        // Action buttons
        findViewById(R.id.btnC).setOnClickListener(v -> onClearClick());
        findViewById(R.id.btnDel).setOnClickListener(v -> onDeleteClick());
        findViewById(R.id.btnPlusMinus).setOnClickListener(v -> onPlusMinusClick());
        findViewById(R.id.btnPercent).setOnClickListener(v -> onPercentClick());
        findViewById(R.id.btnEquals).setOnClickListener(v -> onEqualsClick());
    }

    private void onNumberClick(String digit) {
        if (isNewOperation || "0".equals(currentNumber) || "Error".equals(currentNumber)) {
            currentNumber = digit;
            isNewOperation = false;
        } else {
            currentNumber += digit;
        }
        tvResult.setText(currentNumber);
    }

    private void onDotClick() {
        if (isNewOperation || "Error".equals(currentNumber)) {
            currentNumber = "0.";
            isNewOperation = false;
        } else if (!currentNumber.contains(".")) {
            currentNumber += ".";
        }
        tvResult.setText(currentNumber);
    }

    private void onOperatorClick(String op) {
        if ("Error".equals(currentNumber)) {
            return;
        }

        if (firstOperand != null && operator != null && !isNewOperation) {
            calculateIntermediate();
        } else {
            try {
                firstOperand = Double.parseDouble(currentNumber);
            } catch (Exception e) {
                firstOperand = 0.0;
            }
        }

        operator = op;
        if (firstOperand != null) {
            tvExpression.setText(String.format(Locale.US, "%s %s", formatNumber(firstOperand), operator));
        }
        isNewOperation = true;
    }

    private Double calculate(double a, String op, double b) {
        switch (op) {
            case "+":
                return a + b;
            case "−":
                return a - b;
            case "×":
                return a * b;
            case "÷":
                if (b == 0) {
                    return null;
                }
                return a / b;
            default:
                return null;
        }
    }

    private void calculateIntermediate() {
        try {
            double secondOperand = Double.parseDouble(currentNumber);
            Double result = calculate(firstOperand, operator, secondOperand);

            if (result == null) {
                currentNumber = "Error";
                firstOperand = null;
                operator = null;
            } else {
                firstOperand = result;
                currentNumber = formatNumber(result);
            }
            tvResult.setText(currentNumber);
        } catch (Exception e) {
            currentNumber = "Error";
            tvResult.setText(currentNumber);
        }
    }

    private void onEqualsClick() {
        if (firstOperand != null && operator != null && !"Error".equals(currentNumber)) {
            try {
                double secondOperand = Double.parseDouble(currentNumber);
                tvExpression.setText(String.format(Locale.US, "%s %s %s =",
                        formatNumber(firstOperand), operator, formatNumber(secondOperand)));

                Double result = calculate(firstOperand, operator, secondOperand);

                if (result == null) {
                    currentNumber = "Error";
                    firstOperand = null;
                } else {
                    currentNumber = formatNumber(result);
                    firstOperand = result;
                }
                operator = null;
                isNewOperation = true;
                tvResult.setText(currentNumber);
            } catch (Exception e) {
                currentNumber = "Error";
                tvResult.setText(currentNumber);
            }
        }
    }

    private void onClearClick() {
        currentNumber = "0";
        firstOperand = null;
        operator = null;
        isNewOperation = true;
        tvExpression.setText("");
        tvResult.setText("0");
    }

    private void onDeleteClick() {
        if ("Error".equals(currentNumber)) {
            onClearClick();
            return;
        }

        if (!isNewOperation && !currentNumber.isEmpty()) {
            if (currentNumber.length() == 1 || (currentNumber.length() == 2 && currentNumber.startsWith("-"))) {
                currentNumber = "0";
                isNewOperation = true;
            } else {
                currentNumber = currentNumber.substring(0, currentNumber.length() - 1);
            }
            tvResult.setText(currentNumber);
        }
    }

    private void onPlusMinusClick() {
        if (!"0".equals(currentNumber) && !"Error".equals(currentNumber)) {
            if (currentNumber.startsWith("-")) {
                currentNumber = currentNumber.substring(1);
            } else {
                currentNumber = "-" + currentNumber;
            }
            tvResult.setText(currentNumber);
        }
    }

    private void onPercentClick() {
        if (!"Error".equals(currentNumber)) {
            try {
                double val = Double.parseDouble(currentNumber) / 100.0;
                currentNumber = formatNumber(val);
                tvResult.setText(currentNumber);
                isNewOperation = true;
            } catch (Exception ignored) {
            }
        }
    }

    private String formatNumber(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return "Error";
        }
        if (val == (long) val) {
            return String.format(Locale.US, "%d", (long) val);
        } else {
            DecimalFormat df = new DecimalFormat("#.########");
            return df.format(val);
        }
    }
}
