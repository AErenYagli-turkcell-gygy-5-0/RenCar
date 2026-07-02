# RenCar Renk Sistemi

> Bu dokümanın kaynağı `Rencar.html` tasarım dosyasıdır (Android UI Kit, Material Design 3, tasarım aracı çıktısı `data-props` bloğunda marka kimliği "Okyanus Mavisi" olarak tanımlıdır). Bu dosyadaki renkler `docs/decisions.md` içindeki karara bağlı olarak `app/src/main/java/com/turkcell/rencar/presentation/theme/Color.kt` ve `Theme.kt` dosyalarına birebir uygulanmıştır. Palette değişikliği gerektiğinde önce bu doküman güncellenir, sonra kod değiştirilir.

---

## 1) Marka Rengi (Primary — Okyanus Mavisi)

| Rol | Light | Dark |
|---|---|---|
| primary | `#0B6BCB` | `#4C95F0` |
| onPrimary | `#FFFFFF` | `#FFFFFF` |
| primaryContainer | `#EAF2FC` | `#14233A` |
| onPrimaryContainer | `#0B6BCB` | `#4C95F0` |

Ek gradyan tonu (splash ikon arka planı, dekoratif): `#1E7FE0 → #0B6BCB` (light), `#3B8EF0 → #0B6BCB` (dark). Ayrı bir Compose rolü değildir, yalnızca dekoratif gradyan olarak kullanılır.

## 2) Hata (Error)

| Rol | Light | Dark |
|---|---|---|
| error | `#E5484D` | `#F0575B` |
| onError | `#FFFFFF` | `#FFFFFF` |

`errorContainer` / `onErrorContainer` kaynak tasarımda ayrıca tanımlanmamıştır; Material3 varsayılan (baseline) tonlarında bırakılmıştır (bkz. §6).

## 3) Arka Plan / Yüzey (Background / Surface)

| Rol | Light | Dark |
|---|---|---|
| background | `#F4F6F9` | `#0C0F14` |
| surface (kart, sheet) | `#FFFFFF` | `#171C24` |
| surfaceVariant (ikon kabı, ikincil chip) | `#F1F4F8` | `#1A212A` |
| onBackground / onSurface | `#101620` | `#F3F6FA` |
| onSurfaceVariant (ikincil metin) | `#5C6675` | `#98A2B0` |
| outline (kenarlık) | `#E3E8EF` | `#2A313B` |
| outlineVariant (üçüncül metin/ikon) | `#9AA3AE` | `#6B7480` |

Not: Harita/hero ekranları (Ana Harita, Araç Detay) `#E9EDF2`/`#EEF1F6` (light) ve `#10151B`/`#11161D` (dark) tonlarını arka plan olarak kullanır; bunlar `background` rolünün küçük varyasyonlarıdır, ayrı bir Compose rolüne bağlanmamıştır.

## 4) Genişletilmiş Semantik Renkler (Material3 dışı — `RenCarExtendedColors`)

Material3 `ColorScheme`'in karşılamadığı, kaynak tasarımda tekrarlanan semantik roller `RenCarExtendedColors` adlı özel bir token seti olarak `Theme.kt` içinde `CompositionLocal` ile sağlanır.

### 4.1) Başarı (Success)

| Rol | Light | Dark |
|---|---|---|
| success (metin) | `#1A9E63` | `#34C98A` |
| successContainer (rozet arka planı) | `#E7F4EC` | `#173726` |
| onSuccess | `#FFFFFF` | `#FFFFFF` |
| successAccent (onay rozeti çemberi, ilerleme çubuğu dolgusu — **temadan bağımsız, tek değer**) | `#1FB370` | `#1FB370` |

### 4.2) Uyarı (Warning — temadan bağımsız, tek değer)

| Rol | Değer |
|---|---|
| warning | `#E6A700` |

### 4.3) Araç Kategorisi Vurgu Renkleri (temadan bağımsız, harita pini ve rozetlerde kullanılır)

| Kategori | Değer |
|---|---|
| categoryEconomic ("Ekonomik") | `#F5821F` |
| categoryPremium ("Premium" / "Konfor") | `#7C5CE6` |
| categorySuv ("SUV") | `#E6A700` |
| categoryExtra (haritada 4. pin rengi — kaynak tasarımda ayrı bir etiketle adlandırılmamıştır) | `#0AB5A6` |
| statusInUse ("Kullanımda" durumu) | `#9AA3AE` |

### 4.4) Devre Dışı (Disabled)

| Rol | Light | Dark |
|---|---|---|
| disabledContainer | `#E3E8EF` | `#222A33` |
| disabledContent | `#9AA3AE` | `#6B7480` |

## 5) Kapsam Dışı Bırakılan Renkler

Aşağıdaki renkler `Rencar.html` içinde yalnızca ödeme kartı marka logosu (Visa, Mastercard) çizimi için kullanılmıştır. Üçüncü taraf marka renkleridir, RenCar tema paletine **dahil edilmemelidir**:

- Visa gradyanı: `#1A1F71 → #0B6BCB`
- Mastercard gradyanı: `#EB001B → #F79E1B`

Cüzdan bakiye kartı gibi dekoratif gradyanlar (`#2479DC → #0B5AAE`) de tema rolü değildir; ilgili bileşende yerel olarak tanımlanabilir.

## 6) Bilinen Boşluklar

- Kaynak tasarımda ayrı bir `secondary` / `tertiary` marka rengi tanımlanmamıştır (hiçbir ekranda bu Material3 rolleri ayırt edici biçimde kullanılmamıştır). Bu roller için renk uydurulmamış, Material3 varsayılan (baseline) tonları korunmuştur. Ürüne bu rolleri gerektiren bir bileşen eklendiğinde önce bu doküman güncellenmeli, sonra kod değiştirilmelidir.
- `errorContainer` / `onErrorContainer` için de aynı sebeple Material3 varsayılanı korunmuştur.

## 7) Tipografi

Kaynak tasarımda başlıklar için `Sora` (500/600/700/800), gövde/etiket metni için `Plus Jakarta Sans` (400/500/600/700/800) kullanılmıştır. Bu iki font, `Type.kt` içinde `androidx.compose.ui:ui-text-google-fonts` (indirilebilir Google Fonts) ile kurulmuştur (bkz. `docs/decisions.md` — "Tipografi: İndirilebilir Google Fonts" kararı).

Yalnızca kaynakta `font-family:'Sora'` ile açıkça işaretlenen metinler Sora kullanır; geri kalan her şey konteynerin varsayılanı olan Plus Jakarta Sans'tır. Material3'ün sabit 13 rollük ölçeğine en yakın komşu mantığıyla eşlenmiştir; birebir piksel eşleşmesi garanti edilmez.

| M3 Rolü | Font | Ağırlık | Boyut | Kaynak örnek |
|---|---|---|---|---|
| displaySmall | Sora | ExtraBold (800) | 36sp | Splash marka başlığı (38px) |
| headlineLarge | Sora | ExtraBold (800) | 32sp | Header logo "Rencar" (30px) |
| headlineMedium | Sora | Bold (700) | 28sp | "Tekrar hoş geldin" (27px) |
| headlineSmall | Sora | ExtraBold (800) | 24sp | OTP hane / fiyat (24px, tam eşleşme) |
| titleLarge | Sora | Bold (700) | 22sp | Araç adı, ekran başlığı (19-21px) |
| titleMedium | Sora | Bold (700) | 16sp | "Rezervasyon Onayı" (17-18px) |
| titleSmall | Plus Jakarta Sans | SemiBold (600) | 14sp | Bölüm başlığı "Kiralama planı" (13px/700) |
| bodyLarge | Plus Jakarta Sans | Medium (500) | 16sp | Paragraf metni, CTA buton metni (15.5-16.5px) |
| bodyMedium | Plus Jakarta Sans | Medium (500) | 14sp | İkincil metin (14-14.5px) |
| bodySmall | Plus Jakarta Sans | Medium (500) | 12sp | Meta bilgi (12-12.5px) |
| labelLarge | Plus Jakarta Sans | SemiBold (600) | 14sp | Chip/segment etiketi (13px/600-700) |
| labelMedium | Plus Jakarta Sans | SemiBold (600) | 12sp | Küçük rozet |
| labelSmall | Plus Jakarta Sans | Bold (700) | 11sp | Durum pilleri "MÜSAİT", alt gezinme etiketi (10.5-11px) |
