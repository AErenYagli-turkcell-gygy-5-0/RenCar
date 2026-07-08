# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

# Mimari Kararlar

---

## 2026-07-08 — Ana Ekran Araç Verisi: GET /vehicles Entegrasyonu, Kategori Filtresinin Backend Tipine Göre Yeniden Kurulması, Pusula Konumlandırma Düzeltmesi ve Splash'a Marka Karşılama Sayfası

**Karar (araç verisi):** `HomeState.mockVehicles` kaldırılmıştır. `domain/vehicle/` altında Auth/License
katmanlarıyla aynı desende (`Vehicle`, `VehicleType`, `VehicleError`, `VehicleResult`,
`VehicleRepository`) yeni bir domain sınırı, `data/remote/vehicle/` ve `data/repository/vehicle/`
altında da `GET /vehicles` uç noktasını çağıran karşılıkları eklenmiştir. `HomeViewModel`, Profil
ekranındaki `ScreenStarted`/`loadProfile()` deseniyle aynı şekilde `HomeIntent.ScreenStarted`
üzerinde araçları yükler; `HomeIntent.RetryVehiclesClicked` ile yeniden denenebilir. `HomeState`'e
`isVehiclesLoading`, `hasLoadedVehicles`, `vehiclesErrorMessage` alanları eklenmiş, sabit
`nearbyCount` alanı kaldırılarak yerine `HomeScreen` içinde filtrelenmiş araç listesinden anlık
hesaplanan sayım geçirilmiştir.

**Karar (kategori filtresi):** OpenAPI sözleşmesinde araçlar için yalnızca gövde tipi
(`SEDAN/SUV/HATCHBACK/STATION/MINIVAN`) alanı bulunur; fiyat/segment bazlı bir alan yoktur. Bu
nedenle önceki Ekonomik/Konfor/SUV filtre çipleri kaldırılmış, kullanıcı onayıyla filtre backend'in
gerçek `VehicleType` enum'una birebir taşınmıştır (Tümü/Sedan/SUV/Hatchback/Station/Minivan).
`presentation/component/map/VehicleMarker.kt` içindeki eski `VehicleCategory` enum'ı silinmiş,
Profil ekranındaki `LicenseReviewStatus` kullanım örüntüsüyle tutarlı olarak sunum katmanında
doğrudan `domain.vehicle.VehicleType` kullanılmaya başlanmıştır. Harita üzerindeki fiyat balonu
rengi salt görsel bir ayrım olduğundan (backend'de kategori vurgu rengi kavramı yoktur) mevcut 4
`RenCarExtendedColors` tokenı (`categoryEconomic/categoryPremium/categorySuv/categoryExtra`) 5
tipe dağıtılmış, yalnızca `SUV` isim eşleşmesiyle kendi tokenını korumuştur; yeni bir renk tokenı
eklenmemiştir.

**Karar (pusula düzeltmesi):** `MainActivity`'deki `enableEdgeToEdge()` nedeniyle MapLibre'nin
native `compass` kontrolü `WindowInsets`'ten habersiz kalıp status bar'ın altında/içinde
görünüyordu. Kontrol kaldırılmamış; `RencarMap.kt` içinde `WindowInsets.statusBars` ile okunan
inset değeri, `mapLibreMap.uiSettings.setCompassMargins(...)` çağrısına status bar yüksekliği +
sabit bir boşluk olarak `LaunchedEffect` üzerinden aktarılmıştır.

**Ek not — pusula kalıcı görünürlüğü:** İlk uygulamadan sonra pusulanın hiç görünmediği bildirilmiştir.
Sebebi konumlama değil, MapLibre'nin varsayılan `compassFadeFacingNorth = true` davranışıdır: harita
kuzeye dönükken (bearing 0) pusula birkaç saniye içinde saydamlaşıp gizlenir; önceki (konum hatalı)
halinde yalnızca kullanıcı haritayı parmakla hareket ettirirken oluşan ufak/istemsiz döndürmelerde kısa
süreliğine görünüyordu. `RencarMap.kt` içinde aynı `LaunchedEffect`'e
`mapLibreMap.uiSettings.setCompassFadeFacingNorth(false)` eklenerek pusula, düzeltilmiş konumuyla
kalıcı olarak görünür bırakılmıştır.

**Karar (splash marka sayfası):** Kullanıcının ilettiği tasarım görseline dayanarak, mevcut 3
sayfalık splash onboarding pager'ının başına (index 0) logo + "Rencar" başlığı + slogan içeren yeni
bir "karşılama" sayfası eklenmiştir; alttaki ortak CTA/giriş bölümü ve nokta göstergesi
değiştirilmemiştir. `SplashState.PAGE_COUNT` 3'ten 4'e çıkarılmış, pager ve nokta göstergesi artık
sabit `onboardingPages.size` yerine bu tek kaynaktan beslenmektedir. Sayfa içeriği (ikon kutusu +
başlık + açıklama) daha önce pager `content` lambda'sına gömülüyken, tekrarı önlemek için
`OnboardingPageBody` adında paylaşılan bir private composable'a çıkarılmıştır. Logo için projede
daha önce eklenmiş ama hiçbir yerde kullanılmayan `res/drawable/ic_rencar_car.xml` yeniden
kullanılmıştır; yeni bir görsel varlık eklenmemiştir.

**Gerekçe:**
- `agents.md` §2.2 gereği, backend'de karşılığı olmayan Ekonomik/Konfor segment verisi
  uydurulamayacağından kategori filtresi gerçek `VehicleType` alanına taşınmıştır (kullanıcı
  onayıyla, alternatifi filtre çubuğunu tamamen kaldırmaktı).
- Pusula kaldırılmak yerine konumlandırılmıştır; uygulamanın harita döndürme jesti hâlâ
  kullanılabilir kalmıştır (kullanıcı onayıyla, alternatifi native pusulayı tamamen devre dışı
  bırakmaktı).
- `domain/vehicle` ve `data/*/vehicle` katmanlarının Auth/License ile birebir aynı dosya/isim
  deseninde kurulması, `docs/architecture/mvi-overview.md` kapsamı dışında olsa da mevcut
  repository/Result/Error ayrımı standardını korur.
- `locationLabel`/`distanceLabel` ("Kadıköy çevresinde", "3 dk uzaklıkta") için backend'de ters
  coğrafi kodlama veya mesafe hesaplama uç noktası olmadığından bu alanlar bu kararın kapsamı
  dışında bırakılmış, sabit değerlerini korumuştur; ayrı bir karar gerektirir.
- `VehicleMarker.price` `Int` tipinde kaldığından `pricePerDay` (API'de `number`) tam sayıya
  yuvarlanır; bilinen küçük bir hassasiyet sınırlamasıdır.

**Etkilenen alanlar:**
- `domain/vehicle/` (yeni: `Vehicle.kt`, `VehicleType.kt`, `VehicleError.kt`, `VehicleResult.kt`,
  `VehicleRepository.kt`)
- `data/remote/vehicle/` (yeni: `VehicleApiService.kt`, `dto/VehicleResponseDto.kt`)
- `data/repository/vehicle/ApiVehicleRepository.kt` (yeni)
- `di/NetworkModule.kt`, `di/RepositoryModule.kt`
- `presentation/component/map/VehicleMarker.kt`, `presentation/component/map/RencarMap.kt`
- `presentation/screen/home/` (`HomeState.kt`, `HomeIntent.kt`, `HomeViewModel.kt`, `HomeScreen.kt`,
  `HomeScreenComponents.kt`)
- `presentation/screen/splash/` (`SplashState.kt`, `SplashScreen.kt`)
- `app/src/main/res/values/strings.xml`

---

## 2026-07-07 — Profil Ekranı: Gerçek API Verisi ve Pasif Menü Satırları

**Karar:** Profil ekranı `presentation/screen/profile/` altında MVI dosya yapısıyla eklenecektir.
Kullanıcı adı ve telefon bilgisi `GET /auth/me`, ehliyet doğrulama durumu `GET /license/status`,
çıkış işlemi ise `POST /auth/logout` üzerinden alınacaktır. Çıkış başarılı olduğunda
`SessionTokenHolder.clear()` ile bellek-içi tokenlar temizlenir ve kullanıcı Login ekranına döner.

Profil ekranındaki ödeme yöntemleri, ayarlar, yardım & destek, davet et ve edit alanları bu
iterasyonda yalnızca görsel olarak gösterilir; ayrı route, placeholder ekran veya tıklama davranışı
eklenmez. OpenAPI sözleşmesinde ehliyet sınıfı alanı bulunmadığından ekranda `B sınıfı` gibi
uydurulmuş veri gösterilmez; ehliyet kartı sadece backend durumuna göre metin üretir.

**Gerekçe:**
- Profil ekranı kullanıcıya oturum ve ehliyet durumunu gösteren gerçek bir müşteri ekranıdır; mock
  veri kullanmak mevcut API sözleşmesi varken yanıltıcı olur.
- Menü satırlarını pasif bırakmak, henüz kapsamı tanımlanmamış alt ekranlar için route ve state
  borcu oluşturmadan tasarım görünümünü tamamlar.
- Logout sırasında token temizliği yapılmazsa kullanıcı görsel olarak çıkış yapmış olsa bile korumalı
  endpointlere eski access token ile istek gönderebilir.

**Etkilenen alanlar:**
- `data/remote/auth/`, `data/repository/auth/`, `domain/auth/`
- `presentation/screen/profile/`
- `presentation/screen/home/`, `presentation/navigation/`
- `app/src/main/res/values/strings.xml`

---

## 2026-07-07 — Ana Ekran Haritası: MapLibre Entegrasyonu ve Konum İzni Reddedilirse Fallback Davranışı

**Karar:** `HomeScreen` artık placeholder olmaktan çıkarılıp gerçek bir MVI ekranına dönüştürülecektir.
Harita render'ı için `presentation/component/map/` altında yeni bir `RencarMap` bileşeni MapLibre
Native Android SDK (`org.maplibre.gl:android-sdk`) ile kurulacak; araç fiyat marker'ları MapLibre
Annotation eklentisi (`org.maplibre.gl:android-plugin-annotation-v9`) tabanlı `SymbolLayer` ile
gösterilecektir. Kullanıcı konumu `com.google.android.gms:play-services-location` paketindeki
`FusedLocationProviderClient` ile alınacaktır.

Konum izni reddedilirse harita varsayılan merkezle (`DEFAULT_CENTER`) yüklenmeye devam edecek,
kullanıcı konumu gösterilmeyecek ve kamera kullanıcı konumuna otomatik gitmeyecektir; ekranda nazik
bir uyarı banner'ı gösterilecek ve kullanıcı isterse izni tekrar deneyebilecektir.

**Gerekçe:**
- Projede daha önce hiçbir harita/konum kütüphanesi bulunmuyordu; `RencarMap.kt`/`MapStyle.kt` gibi
  dosyalar bu kararla ilk kez oluşturulmaktadır (kullanıcı onayı ile, bkz. agents.md §2.2).
- MapLibre Native açık kaynak olması ve OSM raster tile stiliyle doğrudan uyumlu olması, Play
  Services Location ise Android'in standart, pil verimli konum API'si olması nedeniyle tercih
  edilmiştir; kütüphane adları ve versiyonları kullanıcı tarafından doğrudan belirlenmiştir.
- Konum izni verilmeden haritanın tamamen kilitlenmesi kötü bir kullanıcı deneyimi oluşturacağından,
  izin reddedildiğinde de harita gösterime devam eder; yalnızca kullanıcıya özel konum bilgisi
  gizlenir.
- Fiyat marker'ları MapLibre Annotation eklentisinin `SymbolManager`/`SymbolOptions` API'si ile
  oluşturulmuş, fiyat metni `SymbolOptions.withTextField` yerine önceden `Canvas` ile çizilip
  `Style.addImage` ile eklenen tek bir bitmap ikonuna gömülmüştür. Sebep: `SymbolManager` üzerinde
  `withTextField` kullanımı bilinen bir görünürlük hatasına sahiptir (bkz. yukarı akış
  `maplibre/maplibre-plugins-android` deposu #60 — text alanı eklenince sembol tamamen kayboluyor).
  Bitmap aynı (kategori, fiyat) kombinasyonu için önbelleğe alınır; kategori başına sınırsız
  bitmap üretimi söz konusu değildir.

**Etkilenen alanlar:**
- `gradle/libs.versions.toml`, `app/build.gradle.kts` (yeni bağımlılıklar: MapLibre SDK, MapLibre
  Annotation, Play Services Location)
- `app/src/main/AndroidManifest.xml` (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
- `presentation/component/map/` (yeni: `MapStyle.kt`, `VehicleMarker.kt`, `RencarMap.kt`)
- `presentation/screen/home/` (`HomeState.kt`, `HomeIntent.kt`, `HomeEffect.kt`, `HomeViewModel.kt`,
  `HomeScreen.kt`, yeni `HomeScreenComponents.kt`)
- `app/src/main/res/values/strings.xml`

---

## 2026-07-06 — Alt Navigasyon İkonlarının Ayrı Bir Dosyada Toplanması

**Karar:** `presentation/component/navigation/BottomNavBar.kt` içinde tanımlı olan, ikonları
`Canvas`/`DrawScope` ile elle çizen `RenCarBottomNavIcon` composable'ı ve ona ait
`iconStroke`/`drawMapPinIcon`/`drawHistoryIcon`/`drawWalletIcon`/`drawProfileIcon` özel
fonksiyonları, aynı paket altında yeni açılan `RenCarIcons.kt` dosyasına taşınmıştır.
`BottomNavBar.kt` artık yalnızca nav bar bileşenini ve önizlemelerini içerir.

Proje genelinde bu karar öncesinde başka bir tarama yapılmış; `LoginScreen`, `RegisterScreen`,
`OtpScreen`, `LicenseUploadScreen` ve `SplashScreen` içindeki tüm `Icon`/`Image` kullanımlarının
`painterResource(id = R.drawable.ic_...)` ile `res/drawable` altındaki XML vector drawable'lara
referans verdiği; bunların Kotlin kodu olmadığı ve zaten Android'in standart kaynak mekanizmasıyla
merkezi tutulduğu için `RenCarIcons.kt`'ye taşınmadığı tespit edilmiştir.
`LicenseUploadScreen.kt` içindeki `Modifier.dashedBorder(...)` bir ikon değil, genel amaçlı
kesikli çerçeve çizen bir Modifier uzantısı olduğundan kapsam dışında bırakılmıştır.

**Gerekçe:**
- İkon çizim mantığı, nav bar'ın layout/state sorumluluğundan bağımsız, tekrar kullanılabilir bir
  görsel varlık kümesidir; ayrı dosyada tutulması dosya boyutunu küçültür ve okunabilirliği artırır.
- `RenCarBottomNavIcon` yalnızca bu paket içinde çağrıldığından `internal`/`private` yerine paket
  içi varsayılan görünürlükle bırakılmış, gereksiz bir erişim genişletmesi yapılmamıştır.
- Elle çizilen (`Canvas`/`DrawScope`) ikonlar ile XML vector drawable tabanlı ikonlar farklı
  tanımlama mekanizmalarıdır; ikincisi zaten `res/drawable/` altında merkezidir ve bu kararın
  kapsamına alınmamıştır.

**Etkilenen alanlar:**
- `presentation/component/navigation/BottomNavBar.kt`
- `presentation/component/navigation/RenCarIcons.kt` (yeni)
## 2026-07-04 - Backend Durumuna Dayalı Ehliyet Doğrulama Akışı

**Karar:** OTP doğrulamasından sonra kullanıcı, backend'in döndürdüğü role göre
yönlendirilecektir. `CUSTOMER` doğrudan Home hedefine, `PENDING` ise
`GET /license/status` ile çözümlenen ehliyet akışına gider. Ehliyet durumu
`NOT_SUBMITTED` veya `REJECTED` ise yükleme, `UNDER_REVIEW` ise onay bekleme,
`APPROVED` ise refresh-token rotation sonrasında Home hedefi açılır.

Ehliyetin ön ve arka yüzü Selfie adımı tamamlanmadan API'ye gönderilmez. Backend
selfie alanı sağlamadığından selfie `TakePicturePreview` ile düşük çözünürlüklü
JPEG byte dizisi olarak yalnızca `LicenseUploadState` içinde tutulur; kalıcı
depolamaya veya ağ isteğine yazılmaz ve ehliyet yüklemesi tamamlanınca temizlenir.

Access ve refresh tokenlar `SessionTokenHolder` içinde yalnızca süreç ömrü boyunca
tutulur. Admin onayından sonra `POST /auth/refresh` çağrılarak yeni `CUSTOMER`
rolünü taşıyan access token alınır. DataStore veya başka bir kalıcı oturum çözümü
bu kararın kapsamı dışındadır.

**Gerekçe:**
- Onaylanmış kullanıcıların tekrar ehliyet yüklemeye yönlendirilmesini engellemek.
- `/license/upload` çağrısının oluşturduğu `UNDER_REVIEW` durumunu backend
  sözleşmesiyle uyumlu zamanda başlatmak.
- Selfie için backend desteği bulunmadığından hassas veriyi kalıcılaştırmamak.
- Eski JWT içindeki `PENDING` rolüyle CUSTOMER uçlarına erişilmesini önlemek.

**Etkilenen alanlar:**
- `data/remote/auth/`, `data/repository/auth/`, `data/session/`
- `domain/license/`, `data/remote/license/`, `data/repository/license/`
- `presentation/screen/auth/otp/`, `presentation/screen/auth/license/`
- `presentation/screen/home/`, `presentation/navigation/`


---

## 2026-07-04 - Register Ekrani ve OTP Akisinin Baslatilmasi

**Karar:** Register ekrani `presentation/screen/auth/register/` altinda MVI dosya yapisiyla
eklenecektir. Ekran `email`, `password`, `fullName` ve 10 haneli Turkiye telefon numarasi alacak;
telefon Login ekranindaki kuralla `+90XXXXXXXXXX` formatina normalize edilerek
`AuthRepository.register()` uzerinden `POST /auth/register` ucuna gonderilecektir. Register
basarili olduktan sonra, backend sozlesmesinde OTP kodunu baslatan uc `POST /auth/login` oldugu
icin ayni telefonla `AuthRepository.requestLogin()` cagrilacak ve basarili cevapla OTP ekranina
gidilecektir.

**Gerekce:**
- `docs/api/openapi.json` icinde `POST /auth/register` basarili cevapta token dondurur; OTP kodu
  baslatma sozlesmesi ise `POST /auth/login` uzerindedir. Bu nedenle sadece register cevabiyla OTP
  ekranina gecmek, kod uretilmeden dogrulama ekranina gitme riski tasir.
- Register sunum katmani, mevcut Login referans implementasyonundaki Route/Screen ayrimi,
  State/Intent/Effect dosya ayrimi ve `MviViewModel` kalibini takip eder.
- Login ekranindaki "Hesabin yok mu? Kayit ol" metni navigation effect uzerinden Register
  route'una baglanir; `Screen` composable'i dogrudan `NavController` bilmez.

**Etkilenen alanlar:**
- `presentation/screen/auth/register/`
- `presentation/screen/auth/login/`
- `presentation/navigation/`
- `app/src/main/res/values/strings.xml`

---

## 2026-07-04 - Ortak Alt Navigasyon Bileseni

**Karar:** Uygulama ekranlarinda tekrar kullanilacak alt navigasyon cubugu, ekran MVI
kontrati acmadan `presentation/component/navigation/` altinda ortak Compose bileseni olarak
tutulacaktir. Bilesen secili item bilgisini disaridan alacak ve item tiklamalarini callback ile
ust katmana bildirecektir.

**Gerekce:**
- Alt navigasyon cubugu tek basina bir ekran/feature degildir; bu nedenle `State/Intent/Effect`
  ve ViewModel dosyalari acilmasi MVI dokumanlarindaki ekran sorumlulugu ile uyumlu olmaz.
- Navigasyon kararinin bilesenin icine gomulmemesi, ayni UI parcaciginin farkli ekran akislariyla
  tekrar kullanilabilmesini saglar.
- Light/Dark tema renkleri `MaterialTheme` uzerinden okunarak mevcut tema kararlarina bagli kalinir.

**Etkilenen dosyalar:**
- `app/src/main/java/com/turkcell/rencar/presentation/component/navigation/RenCarBottomNavBar.kt`

Bu dosya RenCar projesinde alınan mimari/teknik kararların kaydını tutar (bkz. `agents.md` §2.4). Her karar tarih, gerekçe ve etkilenen dosyalarla birlikte eklenir.

---

## 2026-07-04 — Splash Onboarding İçeriğinin Tek Route İçinde Pager Olarak Yönetilmesi

**Karar:** Splash onboarding akışı ayrı navigasyon hedefleri yerine tek `SplashRoute` içinde üç
içerikli `HorizontalPager` olarak gösterilecektir. Geçerli sayfa indeksi `SplashState` içinde
tutulacak ve kullanıcı kaydırmaları `SplashIntent.PageChanged` ile ViewModel'e aktarılacaktır.
Görsel ve metin kaynakları sunum katmanında kalacak; ViewModel yalnızca geçerli indeksi yönetecektir.

**Gerekçe:**
- Üç onboarding içeriği aynı ekran akışının parçalarıdır; ayrı route oluşturmak gereksiz navigasyon
  durumu üretir.
- Sayfa göstergesinin pager ile aynı state kaynağından beslenmesi, statik gösterge ve içerik
  uyumsuzluğunu önler.
- Android resource kimliklerinin State'e taşınmaması ViewModel'i Android UI ayrıntılarından bağımsız
  tutar.

**Etkilenen alanlar:**
- `presentation/screen/splash/`
- `app/src/main/res/values/strings.xml`

## 2026-07-04 — Otp Doğrulama ve Ehliyet Yükleme API Entegrasyonu; Bellek-İçi Oturum Token Yönetimi

**Karar:** `POST /auth/verify-otp` ve `POST /license/upload` uçları entegre edilmiştir.
`POST /auth/verify-otp` başarılı dönüşte backend'in ürettiği `accessToken`, yeni
`data/session/SessionTokenHolder.kt` (`@Singleton`) sınıfında yalnızca bellekte (`@Volatile var`)
tutulur; diske/DataStore'a yazılmaz. `data/remote/AuthInterceptor.kt` adlı bir OkHttp
`Interceptor`, bu token mevcutsa tüm isteklere `Authorization: Bearer` başlığını otomatik ekler;
bu interceptor `di/NetworkModule.kt` içinde kurulan tekil `OkHttpClient`'a bağlanmıştır. Bu şekilde
Otp ekranında elde edilen token, Ehliyet Yükleme ekranındaki `/license/upload` çağrısına
kullanıcı/ViewModel araya girmeden taşınmış olur.

**Kapsam:**
- Otp ekranı artık gerçek bir 6 haneli kod girişi alır (mevcut kutu tasarımının üzerine
  bindirilmiş şeffaf bir `BasicTextField` ile); `VerifyClicked` intent'i telefon + kodu
  `AuthRepository.verifyOtp()` üzerinden `/auth/verify-otp`'a gönderir.
- Kodu tekrar gönder (resend) özelliği eklenmiştir: mevcut `AuthRepository.requestLogin()`
  çağrısı tekrar kullanılır (yeni bir endpoint gerekmez). Kodun geçerlilik süresi backend'den
  ayrıca nav-arg olarak taşınmadığından (Login/NavHost dosyalarına dokunmamak amacıyla),
  istemci tarafında sabit **300 saniyelik** yaklaşık bir geri sayım uygulanır; bu, sunucu
  saatiyle birebir senkron değildir.
- Ehliyet Yükleme ekranında galeri seçimi `androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia`
  ile `LicenseUploadRoute` içinde yapılır (kamera değil, galeri/Photo Picker — kullanıcı onayı
  ile). Seçilen `Uri`, `LicenseUploadIntent.FrontImageSelected`/`BackImageSelected` ile
  ViewModel'e taşınır ve State'te tutulur (`frontImageUri`/`backImageUri`); eski
  `isFrontUploaded`/`isBackUploaded` boolean alanları kaldırılmış, "yüklendi" rozeti artık
  `Uri`'nin null olup olmamasından türetilir.
- Seçilen `Uri`'lerin bayt içeriği (multipart gövde için) `ApiLicenseRepository` içinde,
  `@ApplicationContext` ile enjekte edilen `Context.contentResolver` üzerinden okunur;
  `LicenseUploadViewModel` Android Context'e dokunmaz, yalnızca `Uri` değerini taşır. `Uri`,
  bir Context/yaşam döngüsü nesnesi değil hafif bir değer tipi olduğundan bu, ViewModel'de
  "framework bağımlılığı olmasın" ilkesinin pragmatik bir yorumu olarak kabul edilmiştir
  (kullanıcı onayı ile).
- Galeri seçicinin (`rememberLauncherForActivityResult`) kendisi yalnızca `LicenseUploadRoute`
  içinde kurulur; `LicenseUploadScreen`'in `(state, onIntent)` imzası değişmemiştir — Route,
  kendisine iletilen `onIntent`'i sarmalayarak `FrontUploadClicked`/`BackUploadClicked`
  intent'lerini picker açılışına çevirir, diğer tüm intent'leri doğrudan ViewModel'e iletir.

**Hata politikası:**
- Otp doğrulama: `401` → `AuthError.InvalidOtp` (yeni), diğerleri mevcut Login hata politikasıyla
  aynı şekilde `Network`/`Unexpected`'a düşer.
- Ehliyet yükleme: `400` → `LicenseError.InvalidFile`, `401` → `LicenseError.Unauthorized`,
  `409` → `LicenseError.AlreadyReviewedOrCustomer`, `413` → `LicenseError.FileTooLarge`,
  `IOException` → `Network`, diğerleri → `Unexpected`. Swagger hata gövdeleri için şema
  tanımlanmadığından (bkz. 2026-07-03 kararı) hata metni burada da parse edilmez.

**Token politikası (güncelleme):** 2026-07-03 kararında "kalıcı token yönetimi kapsam dışıdır"
denmişti; bu karar bunu yalnızca **bellek-içi, süreç ömrüyle sınırlı** bir oturuma genişletir.
Kalıcı depolama (ör. DataStore) hâlâ kapsam dışıdır ve ayrı bir karar gerektirir. Bilinen sınır:
uygulama süreci öldürülürse (ör. arka planda sistem tarafından kapatılırsa) token kaybolur ve
Ehliyet Yükleme ekranına dönüldüğünde `401` alınır.

**Gerekçe:**
- Login → Otp → Ehliyet Yükleme akışı tek bir uygulama oturumunda gerçekleştiğinden, bellek-içi
  bir tutucu ek bağımlılık (DataStore vb.) gerektirmeden yeterlidir; bu en az kapsamlı çözümdür
  (kullanıcı onayı ile).
- `AuthResult<T>`/`AuthError` kalıbının Ehliyet için birebir tekrarlanması (`LicenseResult<T>`/
  `LicenseError`), auth ve license özelliklerinin ayrı sınır (`domain/auth`, `domain/license`)
  olarak tutulmasına dair 2026-07-04 (DTO/domain dosya yerleşimi) kararıyla tutarlıdır; ortak bir
  jenerik `Result` tipine geçiş, mevcut `AuthResult` kullanım noktalarını da etkileyecek bir
  sapma olacağından bu kararın kapsamına alınmamıştır.

**Etkilenen alanlar:**
- `data/session/SessionTokenHolder.kt` (yeni), `data/remote/AuthInterceptor.kt` (yeni)
- `di/NetworkModule.kt`, `di/RepositoryModule.kt`
- `gradle/libs.versions.toml`, `app/build.gradle.kts` (yeni ana bağımlılık: `com.squareup.okhttp3:okhttp`)
- `domain/auth/AuthError.kt`, `domain/auth/AuthRepository.kt`, `domain/auth/VerifiedSession.kt` (yeni)
- `data/remote/auth/AuthApiService.kt`, `data/remote/auth/dto/VerifyOtpRequestDto.kt` (yeni)
- `data/repository/auth/ApiAuthRepository.kt`
- `domain/license/` (yeni: `UploadedLicense.kt`, `LicenseError.kt`, `LicenseResult.kt`, `LicenseRepository.kt`)
- `data/remote/license/` (yeni: `LicenseApiService.kt`, `dto/LicenseResponseDto.kt`)
- `data/repository/license/ApiLicenseRepository.kt` (yeni)
- `presentation/screen/auth/otp/` (`OtpState.kt`, `OtpIntent.kt`, `OtpViewModel.kt`, `OtpScreen.kt`)
- `presentation/screen/auth/license/` (`LicenseUploadState.kt`, `LicenseUploadIntent.kt`,
  `LicenseUploadViewModel.kt`, `LicenseUploadScreen.kt`)
- `app/src/main/res/values/strings.xml`

**Nasıl kullanılır:** Bearer token gerektiren yeni bir endpoint eklendiğinde ayrıca bir şey
yapmaya gerek yoktur; `AuthInterceptor` mevcut token'ı otomatik ekler. Token'ı temizlemek
gerekirse (ör. ileride bir çıkış/logout akışında) `SessionTokenHolder.clear()` çağrılmalıdır.

---

## 2026-07-04 — Auth DTO ve Domain Modellerinin Ayrı Dosyalarda Tutulması

**Karar:** Auth katmanındaki her bağımsız DTO ve domain modeli, sınıf adıyla aynı adı taşıyan
ayrı bir Kotlin dosyasında tutulacaktır. Auth uzak veri kaynağına ait servis ve DTO'lar
`data/remote/auth/` feature sınırı altında; DTO'lar bunun `dto/` alt paketinde tutulacaktır.
Auth repository implementasyonu `data/repository/auth/` feature alt paketinde tutulacaktır.
Bir sealed interface'e ait kapalı alt tipler ise ana interface ile aynı dosyada kalacaktır.

**Gerekçe:**
- Model ve DTO tanımlarının konumunu doğrudan sınıf adı üzerinden bulabilmek.
- Auth kapsamı genişledikçe toplu model dosyalarının büyümesini önlemek.
- Birbirine ait sealed interface ve alt tiplerini gereksiz şekilde parçalamamak.

**Etkilenen alanlar:**
- `data/remote/auth/`
- `data/remote/auth/dto/`
- `data/repository/auth/`
- `domain/auth/`
- `app/src/test/java/com/turkcell/rencar/test/MainDispatcherRule.kt` kullanım açıklaması

---

## 2026-07-03 — Auth API Entegrasyonu: Retrofit, Repository Sınırı ve Login MVI Akışı

**Karar:** `POST /auth/register` ve `POST /auth/login` uçları Retrofit 3 + Gson ile entegre
edilecektir. Sunum katmanı Retrofit/HTTP tiplerine doğrudan bağımlı olmayacak; `AuthRepository`
domain arayüzü üzerinden `AuthResult` kullanacaktır. API implementasyonu Hilt ile singleton olarak
bağlanacaktır.

**Kapsam:**
- Register çağrısı data/domain seviyesinde hazırlanır; bu kararda kayıt ekranı eklenmez.
- Login ekranı 10 haneli Türkiye telefon numarasını alır ve API'ye `+90XXXXXXXXXX` olarak gönderir.
- Login başarılıysa backend'in döndürdüğü telefonla mevcut OTP ekranına geçilir.
- OTP doğrulama, refresh, logout, `/auth/me` ve kalıcı token yönetimi kapsam dışıdır.

**Hata politikası:** Swagger hata gövdeleri için bir şema tanımlamadığından hata metni parse
edilmez. Register `409` sonucu `EmailAlreadyRegistered`, login `401` sonucu `UserNotFound`,
`IOException` sonucu `Network`, diğer hatalar `Unexpected` olarak domain katmanına çevrilir.

**Token politikası:** Register cevabındaki access/refresh token alanları wire DTO'da karşılanır;
saklanmaz, loglanmaz ve domain/presentation katmanına aktarılmaz.

**Gerekçe:**
- Repository sınırı, backend/Retrofit ayrıntılarının MVI ViewModel'lerine sızmasını engeller.
- Retrofit servisi ve repository binding'lerinin Hilt tarafından sağlanması mevcut DI standardıyla
  uyumludur.
- `BuildConfig.API_BASE_URL`, ortam adresinin ağ modülünden ayrılmasını sağlar.

**Etkilenen alanlar:**
- `gradle/libs.versions.toml`, `app/build.gradle.kts`, `AndroidManifest.xml`
- `data/remote/`, `data/repository/`, `domain/auth/`, `di/`
- `presentation/screen/auth/login/`, `presentation/navigation/RenCarDestination.kt`
- Auth repository ve Login ViewModel unit testleri

---

## 2026-07-03 — MVI Kontrat Dosyalarının State / Intent / Effect Olarak Ayrılması

**Karar:** Ekran bazlı MVI kontrat tanımları artık tek `<Feature>Contract.kt` dosyasında değil,
ayrı `<Feature>State.kt`, `<Feature>Intent.kt` ve `<Feature>Effect.kt` dosyalarında tutulacaktır.
Splash, Login ve Otp referans ekranları bu yapıya taşınmıştır.

**Gerekçe:**
- State, Intent ve Effect tiplerinin ayrı dosyalarda tutulması ekran paketlerinde sorumlulukları
  daha net hale getirir.
- Yeni ekranlarda ilgili kontrat parçasına doğrudan erişim kolaylaşır ve dosya boyutu
  büyüdükçe okunabilirlik korunur.
- Bu karar sunum katmanındaki MVI davranışını değiştirmez; yalnızca dosya yerleşimi standardını
  günceller.

**Etkilenen dosyalar:**
- `docs/architecture/mvi-contracts.md`
- `docs/architecture/mvi-overview.md`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/login/LoginState.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/login/LoginIntent.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/login/LoginEffect.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/login/LoginContract.kt` (silindi)
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/otp/OtpState.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/otp/OtpIntent.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/otp/OtpEffect.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/otp/OtpContract.kt` (silindi)
- `app/src/main/java/com/turkcell/rencar/presentation/screen/splash/SplashState.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/splash/SplashIntent.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/splash/SplashEffect.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/splash/SplashContract.kt` (silindi)

**Nasıl kullanılır:** Yeni ekranlarda State, Intent ve Effect ayrı dosyalarda tanımlanır.
ViewModel ve Screen dosyaları aynı paketteki bu tipleri kullanmaya devam eder.

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

---

## 2026-07-02 — Sunum Katmanı: MVI Kontratının Tanımlanması ve Hilt ile DI

**Karar:** `agents.md` §2.4'te bağlayıcı referans olarak belirtilen `docs/architecture/mvi-overview.md`, `docs/architecture/mvi-contracts.md` ve `docs/architecture/mvi-viewmodel-rules.md` dosyaları o ana kadar boştu; bu kararla ilk kez içerik kazanmıştır. MVI, `UiState`/`UiIntent`/`UiEffect` marker interface'leri ve tek bir `MviViewModel<S, I, E>` taban sınıfı üzerine kurulu en sade haliyle tanımlanmıştır. Ekranlar arası bağımlılık enjeksiyonu için **Hilt** (`hilt-android`, `hilt-navigation-compose`) seçilmiştir. Ekranlar arası geçiş için `androidx.navigation:navigation-compose` ile bir `NavHost` (`presentation/navigation/RenCarNavHost.kt`) kurulmuştur. Splash, Login ve Otp ekranları bu kontratın ilk referans implementasyonlarıdır.

**Gerekçe:**
- Projede daha önce hiç ViewModel/State yönetimi, DI çözümü veya navigasyon kütüphanesi bulunmuyordu; `LoginScreen`, `SplashOnboardingScreen` ve `OtpVerificationScreen` tamamen stateless, statik veri gösteren composable'lardı. `agents.md`'nin "referans implementasyon Login ekranıdır" ifadesi bu haliyle karşılanamıyordu; bu karar bu boşluğu kapatmaktadır.
- MVI kontratının şekli (`docs/architecture/*.md` boş olduğundan) uydurulmadan önce kullanıcıya sunulmuş, taslak onaylandıktan sonra bu dosyalara yazılmıştır (bkz. `agents.md` §2.2).
- Hilt, Google'ın resmi Android DI çözümü olması ve Jetpack Compose ile `hiltViewModel()` üzerinden birebir entegre olması nedeniyle manuel DI ve Koin'e tercih edilmiştir (kullanıcı onayı ile).
- State/Intent/Effect için ayrı sealed hiyerarşiler yerine tek bir `data class` State ve `sealed interface` Intent/Effect tercih edilmiştir; bu, "en sade haliyle MVI" hedefiyle uyumludur ve gereksiz soyutlama eklemez.
- Ekranlardaki telefon numarası ve OTP hane alanları şu an gerçek bir `TextField` girişi değil, statik `Box`'lardır; bu kapsamda yalnızca State'ten okunacak şekilde bağlanmış, gerçek klavye/giriş davranışının eklenmesi ayrı bir karara bırakılmıştır. Backend/API entegrasyonu ve domain/data katmanları da bu kararın kapsamı dışındadır.

**Etkilenen dosyalar:**
- `docs/architecture/mvi-overview.md`, `docs/architecture/mvi-contracts.md`, `docs/architecture/mvi-viewmodel-rules.md` (ilk kez dolduruldu)
- `gradle/libs.versions.toml`, `app/build.gradle.kts`, `build.gradle.kts` (Hilt, KSP, Navigation Compose, Hilt Navigation Compose, Lifecycle Compose bağımlılıkları)
- `app/src/main/java/com/turkcell/rencar/RenCarApplication.kt` (yeni), `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/turkcell/rencar/presentation/core/mvi/` (yeni: `UiState.kt`, `UiIntent.kt`, `UiEffect.kt`, `MviViewModel.kt`)
- `app/src/main/java/com/turkcell/rencar/presentation/navigation/` (yeni: `RenCarDestination.kt`, `RenCarNavHost.kt`)
- `app/src/main/java/com/turkcell/rencar/MainActivity.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/splash/` (`SplashContract.kt`, `SplashViewModel.kt` yeni; `SplashScreen.kt` güncellendi)
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/login/` (`LoginContract.kt`, `LoginViewModel.kt` yeni; `LoginScreen.kt` güncellendi)
- `app/src/main/java/com/turkcell/rencar/presentation/screen/auth/otp/` (`OtpContract.kt`, `OtpViewModel.kt` yeni; `OtpScreen.kt` güncellendi)

**Nasıl kullanılır:** Yeni bir ekran eklenirken `docs/architecture/mvi-overview.md`, `mvi-contracts.md` ve `mvi-viewmodel-rules.md` bağlayıcı referans olarak izlenir; Route/Screen ayrımı ve `hiltViewModel()` kalıbı birebir uygulanır.

**Bilinen risk:** AGP 9.2.1'in varsayılan olarak etkin "built-in Kotlin" derleme modeli, KSP'nin (Hilt annotation processing) üretilen kaynakları `kotlin.sourceSets` DSL'i üzerinden eklemesiyle çakışıyor ve `compileDebugKotlin` sırasında "Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin" hatası veriyordu (bkz. yukarı akış `google/ksp` deposu #2729, #2615 — AGP 9.2.1 / KSP 2.2.10-2.0.2 kombinasyonunda araştırma sırasında net biçimde çözülmüş bulunamadı). Kullanıcı onayıyla `gradle.properties` içine `android.disallowKotlinSourceSets=false` eklenerek bu hata dar kapsamlı olarak bastırılmıştır; bu ayar Gradle tarafından "experimental" olarak işaretlenmektedir ve resmi Android dokümantasyonu bunun kalıcı kullanımını önermemektedir. KSP/AGP tarafında bu uyumsuzluk resmi olarak giderildiğinde bu satır `gradle.properties`'ten kaldırılmalıdır.

**Ek etkilenen dosya:** `gradle.properties`

**Ek not — compileSdk 36 → 37:** `./gradlew :app:assembleDebug` sırasında `checkDebugAarMetadata` görevi başarısız oldu; hem bu kararla eklenen `androidx.lifecycle:lifecycle-runtime-compose`/`lifecycle-viewmodel-compose:2.11.0` hem de bu kararla ilgisiz, projede önceden mevcut olan `androidx.core:core-ktx:1.19.0` bağımlılığı `compileSdk 37` talep ediyordu (proje `compileSdk 36` idi). Kullanıcı onayıyla `app/build.gradle.kts`'te yalnızca `compileSdk` `37`'ye çıkarılmıştır (`targetSdk`/`minSdk` değişmemiştir, çalışma zamanı davranışında değişiklik yoktur). Bu değişiklik olmadan proje zaten APK üretemiyordu; bu nedenle MVI kararının bir parçası olarak kayda geçirilmiştir. **Ek etkilenen dosya:** `app/build.gradle.kts` (`compileSdk` bloğu).
