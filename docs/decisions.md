# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

# Mimari Kararlar

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
