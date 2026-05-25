# UML Editor Future Features TODO

這份文件整理目前專案後續可以新增的功能，作為下一階段開發待辦清單。

## 1. 存檔與讀檔

- [x] 設計可序列化的專案資料格式，例如 JSON。
- [x] 儲存所有 UML 物件資訊：
  - [x] 物件類型：Class / Interface
  - [x] 位置：x, y
  - [x] 尺寸：width, height
  - [x] 名稱
  - [x] attributes
  - [x] methods
- [x] 儲存所有關係線資訊：
  - [x] 起點物件與 port
  - [x] 終點物件與 port
  - [x] 關係線類型
- [x] 加入 Save / Open UI 操作。
- [x] 處理讀檔時的錯誤格式與相容性問題。

## 2. 匯出圖片或文件

- [ ] 支援將目前畫布匯出成 PNG。
- [ ] 支援只匯出有內容的有效區域，而不是整張空白畫布。
- [ ] 可選擇是否匯出成 PDF。
- [ ] 加入 Export UI 操作。
- [ ] 匯出前確認關係線、文字與選取框的顯示狀態是否符合預期。

## 3. 新增 Association 關聯線

- [x] 在 `LineType` 中新增 `ASSOCIATION`。
- [x] 在工具列加入 Association 按鈕。
- [x] 實作一般實線關聯線。
- [ ] 支援可選箭頭方向。
- [ ] 支援多重性標籤，例如 `1`, `0..1`, `1..*`。
- [ ] 支援關係名稱標籤。

## 4. 更完整的 UML 成員編輯

- [ ] 擴充 `UMLAttribute` 欄位：
  - [ ] visibility，例如 `+`, `-`, `#`, `~`
  - [ ] name
  - [ ] type
  - [ ] default value
- [ ] 擴充 `UMLMethod` 欄位：
  - [ ] visibility
  - [ ] name
  - [ ] parameters
  - [ ] return type
- [ ] 更新右側 Properties TreeView 的編輯方式。
- [ ] 更新 Class / Interface 的繪製格式。
- [ ] 加入基本輸入驗證，避免空名稱或格式錯誤。

## 5. Undo / Redo

- [ ] 建立 Command Pattern 基礎介面。
- [ ] 將新增物件包裝成 command。
- [ ] 將刪除物件與刪除線條包裝成 command。
- [ ] 將拖曳移動包裝成 command。
- [ ] 將 resize 包裝成 command。
- [ ] 將屬性與方法編輯包裝成 command。
- [ ] 加入 Undo / Redo 快捷鍵：
  - [ ] Ctrl+Z
  - [ ] Ctrl+Y 或 Ctrl+Shift+Z
- [ ] 在 UI 上加入 Undo / Redo 按鈕。

## 6. ScrollPane 與 Zoom

- [ ] 將 `UMLCanvas` 放入 `ScrollPane`。
- [ ] 支援畫布擴張後可以水平與垂直捲動。
- [ ] 加入 zoom in / zoom out。
- [ ] 加入 fit-to-screen。
- [ ] 確保滑鼠座標在縮放後仍能正確對應到畫布座標。
- [ ] 測試建立物件、拖曳、resize、連線在不同縮放比例下是否正常。

## 7. 自動排版與對齊輔助

- [ ] 加入 grid 背景。
- [ ] 支援 snap to grid。
- [ ] 拖曳時顯示對齊輔助線。
- [ ] 支援多選物件。
- [ ] 支援多選後一起移動。
- [ ] 支援 align left / right / top / bottom / center。
- [ ] 支援平均分布 selected objects。
- [ ] 評估是否加入簡單 auto layout。

## 建議開發順序

1. 新增 Association 關聯線。
2. 實作存檔與讀檔。
3. 實作匯出 PNG。
4. 擴充 UMLAttribute / UMLMethod。
5. 加入 ScrollPane 與 Zoom。
6. 加入 Undo / Redo。
7. 加入自動排版與對齊輔助。
