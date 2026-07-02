# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

# Mimari Kararlar

Bu dosya RenCar projesinde alınan mimari/teknik kararların kaydını tutar (bkz. `agents.md` §2.4). Her karar tarih, gerekçe ve etkilenen dosyalarla birlikte eklenir.

---

## 2026-07-02 — Renk Sistemi: Dynamic Color Kapalı, Genişletilmiş Özel Renk Seti

**Karar:** `RenCarTheme` bileşeninde `dynamicColor` parametresinin varsayılan değeri `false` olarak ayarlanmıştır. Ayrıca Material3 `ColorScheme`'in karşılamadığı semantik/kategori renkleri (başarı, uyarı, araç kategorisi vurgu renkleri, devre dışı durumu) için `RenCarExtendedColors` adında ayrı bir `CompositionLocal` tabanlı token seti eklenmiştir.

**Gerekçe:**
- `Rencar.html` tasarım kaynağında ("Okyanus Mavisi" marka kimliği) bilinçli ve sabit bir renk paleti tanımlanmıştır. Android 12+ cihazlarda `dynamicColor = true` varsayılanı, kullanıcının duvar kağıdından türeyen renklerin bu marka kimliğinin üzerine geçmesine yol açar; bu istenmeyen bir davranıştır.
- Kaynak tasarımda başarı (success), uyarı (warning) ve araç kategorisi (Ekonomik/Premium/SUV) renkleri Material3'ün `primary`/`secondary`/`tertiary`/`error` rollerine doğal olarak oturmamaktadır. Bu renkleri zorla mevcut rollere sıkıştırmak yerine ayrı, anlamlı isimlere sahip bir genişletilmiş renk seti tanımlanmıştır. Bu yaklaşım `docs/design/00-color-system.md` içinde detaylandırılmıştır.
- Kaynakta ayrı bir `secondary`/`tertiary` marka rengi bulunmadığından bu roller uydurulmamış, Material3 varsayılan (baseline) tonlarında bırakılmıştır (bkz. `docs/design/00-color-system.md` §6).

**Etkilenen dosyalar:**
- `docs/design/00-color-system.md` (yeni renk sistemi dokümanı)
- `app/src/main/java/com/turkcell/rencar/presentation/theme/Color.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/theme/Theme.kt`

**Nasıl kullanılır:** Material3 rolleri her zamanki gibi `MaterialTheme.colorScheme.*` üzerinden; genişletilmiş roller `MaterialTheme.extendedColors.*` üzerinden erişilir (örn. `MaterialTheme.extendedColors.categoryEconomic`).

---

## 2026-07-02 — Tipografi: İndirilebilir Google Fonts (Sora + Plus Jakarta Sans)

**Karar:** `Type.kt` içindeki `Typography`, `Rencar.html` kaynağında kullanılan `Sora` (başlıklar) ve `Plus Jakarta Sans` (gövde/etiket) yazı tipleriyle, Jetpack Compose'un `androidx.compose.ui:ui-text-google-fonts` indirilebilir font API'si üzerinden kurulmuştur.

**Gerekçe:**
- Kaynak tasarımda her iki yazı tipi de Google Fonts üzerinden yüklenmektedir (`fonts.googleapis.com` bağlantısı). Statik `.ttf` dosyası projeye eklenmediğinden ve bu dosyalar elde mevcut olmadığından, resmi Compose "downloadable fonts" mekanizması tercih edilmiştir; bu şekilde font dosyası ikili varlık olarak depoya eklenmemiştir.
- `com.google.android.gms:play-services-fonts` ayrı bir Gradle bağımlılığı olarak eklenmemiştir; resmi Android dokümantasyonuna göre font isteği cihazdaki Google Play Services APK'sı üzerinden yürütülür, derleme zamanı bağımlılığı gerektirmez.
- `app/src/main/res/values-v23/font_certs.xml` içeriği ezberden yazılmamış, `android/compose-samples` (Jetchat örneği) resmi deposundaki `font_certs.xml` dosyasından birebir alınmıştır (bkz. dosya içi kaynak yorumu).
- Material3'ün sabit 13 rollük tipografi ölçeği, kaynaktaki serbest piksel boyutlarına en yakın komşu mantığıyla eşlenmiştir; birebir piksel eşleşmesi garanti edilmez. Eşleme tablosu `docs/design/00-color-system.md` §7'de tutulur.

**Etkilenen dosyalar:**
- `gradle/libs.versions.toml`, `app/build.gradle.kts` (yeni bağımlılık: `androidx-compose-ui-text-google-fonts`)
- `app/src/main/res/values-v23/font_certs.xml` (yeni dosya)
- `app/src/main/java/com/turkcell/rencar/presentation/theme/Type.kt`

**Bilinen risk:** Google Play Services yüklü olmayan/eski cihazlarda veya ilk açılışta internet bağlantısı yoksa font indirmesi başarısız olabilir; bu durumda Compose sistem varsayılan fontuna geri döner. Bu davranış test edilmemiştir, gerçek cihazda doğrulanması önerilir.
