package com.hoanglong171.movies.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.hoanglong171.movies.R;

public class VideoPlayerActivity extends AppCompatActivity {
    private PlayerView playerView;
    private ExoPlayer player;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        playerView = findViewById(R.id.playerView);

        // Lấy videoUrl từ Intent
        videoUrl = getIntent().getStringExtra("videoUrl");
        if (videoUrl == null || videoUrl.isEmpty()) {
            finish(); // Đóng Activity nếu videoUrl không hợp lệ
            return;
        }

        // Khởi tạo ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Tạo MediaItem từ videoUrl
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);

        // Chuẩn bị và phát video
        player.prepare();
        player.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause(); // Tạm dừng video khi Activity bị pause
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play(); // Tiếp tục phát video khi Activity được resume
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release(); // Giải phóng ExoPlayer khi Activity bị hủy
            player = null;
        }
    }
}