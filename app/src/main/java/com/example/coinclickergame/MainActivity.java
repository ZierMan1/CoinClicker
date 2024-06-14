package com.example.coinclickergame;

import android.widget.Button;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;




public class MainActivity extends AppCompatActivity {

    private int score = 0;
    private TextView scoreText;
    private TextView gameOverText;
    private TextView timerText;
    private Random random = new Random();
    private List<ImageView> coins = new ArrayList<>();
    private ImageView correctCoin;
    private int correctCoinNumber;
    private Handler handler = new Handler();
    private LinearLayout coinContainer;
    private CountDownTimer timer;
    private Button restartButton;
    private MediaPlayer backgroundMusic;
    private ImageView correctCoinNumberImage;
    private TextView startGamePrompt;
    private int[] buttonImages = {
            R.drawable.button_1, R.drawable.button_2, R.drawable.button_3,
            R.drawable.button_4, R.drawable.button_5, R.drawable.button_6,
            R.drawable.button_7, R.drawable.button_8, R.drawable.button_9,
            R.drawable.button_10, R.drawable.button_11
    };
    private ImageView musicToggleButton;
    private boolean isMusicPlaying = true; // Предполагаем, что музыка играет при запуске


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backgroundMusic = MediaPlayer.create(this, R.raw.music);
        backgroundMusic.setLooping(true);
        backgroundMusic.start();

        scoreText = findViewById(R.id.scoreText);
        gameOverText = findViewById(R.id.gameOverText);
        timerText = findViewById(R.id.timerText);
        restartButton = findViewById(R.id.restartButton);
        coinContainer = findViewById(R.id.coinContainer);
        startGamePrompt = findViewById(R.id.startGamePrompt);
        restartButton.setOnClickListener(view -> restartGame());
        correctCoinNumberImage = findViewById(R.id.correctCoinNumberImage); // Находим ImageView в макете

        musicToggleButton = findViewById(R.id.musicToggleButton);
        updateMusicButton(); // Устанавливаем начальное изображение кнопки

        musicToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMusicPlaying) {
                    backgroundMusic.pause();
                } else {
                    backgroundMusic.start();
                }
                isMusicPlaying = !isMusicPlaying; // Переключаем состояние
                updateMusicButton(); // Обновляем изображение кнопки
            }
        });

        startGamePrompt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame();
            }
        });



        resetCoins();
    }


    private void startGame() {
        startGamePrompt.setVisibility(View.GONE); // Скрываем текст с приглашением
        resetCoins();
        startTimer(); // Теперь запускаем таймер
    }
    private void updateMusicButton() {
        if (isMusicPlaying) {
            musicToggleButton.setImageResource(R.drawable.headphones_black);
        } else {
            musicToggleButton.setImageResource(R.drawable.headphones_red);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundMusic != null) {
            backgroundMusic.release();
            backgroundMusic = null;
        }
    }

    private void startTimer() {
        timer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Осталось: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                endGame();
            }
        }.start();
    }

    private void resetCoins() {
        coinContainer.removeAllViews();
        coins.clear();

        int newCoinsCount = 5 + score; // Количество монет зависит от текущего счета
        correctCoinNumber = random.nextInt(newCoinsCount); // Выбираем номер правильной монеты

        // Выбираем случайное изображение для правильной монеты
        final int correctImageResId = buttonImages[random.nextInt(buttonImages.length)];
        correctCoinNumberImage.setImageResource(correctImageResId); // Устанавливаем изображение правильной монеты

        coinContainer.post(new Runnable() {
            @Override
            public void run() {
                int containerWidth = coinContainer.getWidth();
                int containerHeight = coinContainer.getHeight();

                for (int i = 0; i < newCoinsCount; i++) {
                    ImageView coin = new ImageView(MainActivity.this);
                    int imageResId;

                    if (i == correctCoinNumber) {
                        // Эта монета будет правильной
                        imageResId = correctImageResId;
                        correctCoin = coin; // Сохраняем ссылку на правильную монету
                    } else {
                        // Для остальных монет выбираем любое изображение, кроме правильного
                        do {
                            imageResId = buttonImages[random.nextInt(buttonImages.length)];
                        } while (imageResId == correctImageResId);
                    }

                    coin.setImageResource(imageResId);
                    coin.setOnClickListener(MainActivity.this::checkCoin);

                    // Устанавливаем размеры коина
                    int size = getResources().getDimensionPixelSize(R.dimen.coin_size); // Получаем размер из ресурсов
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
                    coin.setLayoutParams(layoutParams);

                    // Рассчитываем и устанавливаем положение монеты
                    int coinX = random.nextInt(containerWidth - size);
                    int coinY = random.nextInt(containerHeight - size);
                    coins.add(coin);
                    coinContainer.addView(coin);
                    coin.setX(coinX);
                    coin.setY(coinY);

                    animateCoin(coin); // Вызываем анимацию для монеты
                }
            }
        });
    }

    private void checkCoin(View view) {
        ImageView clickedCoin = (ImageView) view;
        if (clickedCoin.equals(correctCoin)) {
            score++;
            scoreText.setText("Score: " + score);
            resetCoins();

            // Перезапускаем таймер
            if (timer != null) {
                timer.cancel();
            }
            startTimer();
        } else {
            endGame();
        }
    }
    private void animateCoin(final ImageView coin) {
        final int maxDistance = 60; // Максимальное расстояние для одного шага анимации
        final long delayMillis = 100; // Задержка между шагами анимации
        final int shakeDistance = 20; // Увеличенная дистанция для более сильной тряски
        final long shakeDuration = 100; // Увеличенная продолжительность тряски

        final Runnable moveRunnable = new Runnable() {
            float dx = random.nextFloat() * maxDistance * 2 - maxDistance;
            float dy = random.nextFloat() * maxDistance * 2 - maxDistance;

            @Override
            public void run() {
                // Получаем текущие координаты монеты
                float currentX = coin.getX();
                float currentY = coin.getY();

                // Рассчитываем новые координаты
                float newX = currentX + dx;
                float newY = currentY + dy;

                // Получаем размеры экрана
                int screenWidth = coinContainer.getWidth();
                int screenHeight = coinContainer.getHeight();

                // Проверяем границы экрана и меняем направление, если монета достигла края
                if (newX <= 0 || newX + coin.getWidth() > screenWidth) {
                    dx = -dx;
                }
                if (newY <= 0 || newY + coin.getHeight() > screenHeight) {
                    dy = -dy;
                }

                newX = Math.max(0, Math.min(newX, screenWidth - coin.getWidth()));
                newY = Math.max(0, Math.min(newY, screenHeight - coin.getHeight()));

                // Обновляем положение монеты
                coin.setX(newX);
                coin.setY(newY);

                // Тряска
                coin.animate().translationXBy(random.nextFloat() * shakeDistance - shakeDistance / 2).setDuration(shakeDuration);
                coin.animate().translationYBy(random.nextFloat() * shakeDistance - shakeDistance / 2).setDuration(shakeDuration);

                // Планируем следующее обновление
                handler.postDelayed(this, delayMillis);
            }
        };

        // Запускаем анимацию
        handler.post(moveRunnable);
    }

    private void endGame() {
        if (timer != null) {
            timer.cancel();
        }
        gameOverText.setVisibility(View.VISIBLE);
        timerText.setVisibility(View.GONE);
        gameOverText.setText("Игра окончена! Ваш счет: " + score);
        restartButton.setVisibility(View.VISIBLE);
    }

    private void restartGame() {
        score = 0;
        scoreText.setText("Score: " + score);
        gameOverText.setVisibility(View.GONE);
        timerText.setVisibility(View.VISIBLE);
        restartButton.setVisibility(View.GONE);

        // Останавливаем текущий таймер, если он активен
        if (timer != null) {
            timer.cancel();
        }

        resetCoins();
        startTimer(); // Запускаем новый таймер на 10 секунд
    }
}