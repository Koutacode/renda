# Renda（Android/Kotlin）

アプリ内のカウンタを「手動タップ」または「自動連打」で増やす、練習/テスト用のミニアプリです。

## 重要
- 他アプリ（TikTok等）を操作する機能はありません。
- 端末上の別アプリを自動操作して不正に「いいね」等を稼ぐ用途の実装には対応できません。

## 使い方
- 画面中央の「タップエリア」を押すと、押した場所が「連打位置」になり、カウントが増えます。
- `自動連打` をONにすると、アプリ内のタップエリアだけを指定速度で自動連打します。

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
