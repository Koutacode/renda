# Renda（Android/Kotlin）

アプリ内のカウンタを「手動タップ」または「自動連打」で増やす、練習/テスト用のミニアプリです。

## 重要
- 他アプリ（TikTok等）を操作する機能はありません。
- 端末上の別アプリを自動操作して不正に「いいね」等を稼ぐ用途の実装には対応できません。

## ビルド（debug APK）
```powershell
.\gradlew.bat clean assembleDebug
```

生成物:
- `app\build\outputs\apk\debug\app-debug.apk`

## output へコピー（配布用ファイル名固定）
```powershell
.\scripts\build_debug.ps1
```

生成物:
- `output\renda-debug.apk`

