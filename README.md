# RenCar

RenCar; araç kiralama ve dakikalık/saatlik/günlük araç paylaşımı süreçlerini yöneten, Android
Jetpack Compose ile geliştirilmiş bir mobil istemci uygulamasıdır. Uygulama, harita üzerinden
müsait araçların keşfedilmesinden rezervasyon, ehliyet doğrulama, teslim alma, aktif yolculuk
takibi ve kiralamanın tamamlanmasına kadar uçtan uca kullanıcı akışını kapsar.

---

## 1) İçindekiler

- [Genel Bakış](#2-genel-bakış)
- [Teknoloji Yığını](#3-teknoloji-yığını)
- [Mimari](#4-mimari)
- [Proje Yapısı](#5-proje-yapısı)
- [Uygulama Akışları ve Ekranlar](#6-uygulama-akışları-ve-ekranlar)
- [Backend Entegrasyonu](#7-backend-entegrasyonu)
- [Gerekli İzinler](#8-gerekli-i̇zinler)
- [Kurulum ve Çalıştırma](#9-kurulum-ve-çalıştırma)
- [Test](#10-test)
- [Dokümantasyon ve Yönetişim](#11-dokümantasyon-ve-yönetişim)
- [Bilinen Sınırlamalar](#12-bilinen-sınırlamalar)

---

## 2) Genel Bakış

RenCar, kullanıcının konumuna yakın müsait araçları harita üzerinde göstererek başlayan bir araç
paylaşım deneyimi sunar. Uygulama; kimlik doğrulama, ehliyet doğrulama, araç keşfi, rezervasyon,
kiralama başlatma (teslim fotoğrafları), aktif yolculuk takibi (canlı konum ve anlık ücret) ve
kiralama geçmişi olmak üzere birbirine bağlı modüllerden oluşur. Uygulama yalnızca istemci
tarafını içerir; iş mantığı ve veri kalıcılığı ayrı bir ekip tarafından işletilen bir RESTful API
ile Socket.IO tabanlı canlı konum servisi üzerinden sağlanır.

## 3) Teknoloji Yığını

| Katman | Teknoloji |
| --- | --- |
| Dil | Kotlin 2.2.10 |
| UI | Jetpack Compose (Material 3) |
| Mimari (sunum katmanı) | MVI (Model-View-Intent) |
| Dependency Injection | Hilt 2.59.2 |
| Network Client | Retrofit + OkHttp (Gson converter) |
| Live Location | Socket.IO Client 2.1.1 |
| Harita | MapLibre Android SDK (+ Annotation eklentisi) |
| Location Services | Google Play Services Location |
| Navigasyon | Navigation Compose |
| Build System | Gradle 9.4.1, Android Gradle Plugin 9.2.1, KSP 2.2.10-2.0.2 |
| Minimum / Target SDK | minSdk 24 / targetSdk 36 / compileSdk 37 |
| Test | JUnit, kotlinx-coroutines-test, OkHttp MockWebServer, Espresso, Compose UI Test |

Backend, farklı bir ekip tarafından geliştirilen ve işletilen bağımsız bir RESTful API'dir; bu
repository yalnızca istemci (Android) kaynak kodunu içerir.

## 4) Mimari

Sunum katmanı, tek yönlü veri akışı (unidirectional data flow) sağlayan **MVI (Model-View-Intent)**
deseniyle yazılmıştır ve bu kural projede bağlayıcıdır. Her ekran/özellik aşağıdaki döngüyü izler:

```
Kullanıcı Aksiyonu -> Intent -> ViewModel.onIntent() -> State güncellenir / Effect tetiklenir
                                                              |
                                                              v
                                                    UI, State'i gözlemleyip yeniden çizilir
                                                    UI, Effect'i bir kereliğine tüketir
```

- **State**: Ekranın o anki tam durumunu temsil eden, immutable, tek bir `data class`.
- **Intent**: Kullanıcının veya sistemin tetiklediği niyet/olay.
- **Effect**: State'e ait olmayan, bir kereye mahsus yan etki (navigasyon, snackbar vb.).

Her ekran kendi paketinde aşağıdaki dosya kümesiyle temsil edilir
(`presentation/screen/<feature>/`):

```
presentation/screen/<feature>/
    <Feature>State.kt      // Ekranın immutable UI state modeli
    <Feature>Intent.kt     // Kullanıcı/sistem olayları
    <Feature>Effect.kt     // Tek seferlik yan etkiler
    <Feature>ViewModel.kt  // MviViewModel<State, Intent, Effect> uygulaması, @HiltViewModel
    <Feature>Screen.kt     // Route (stateful) + Screen (stateless) composable'lar
```

Çekirdek MVI soyutlamaları `presentation/core/mvi/` altında tüm özellikler arasında paylaşılır.
Referans implementasyon Login, Splash ve Otp ekranlarıdır; yeni bir ekran eklenirken bu ekranların
paket/dosya yapısı örnek alınır.

Katmanlar genel hatlarıyla şu şekilde ayrılır:

- **domain/** — İş kurallarından bağımsız modeller ve repository sözleşmeleri (`auth`, `license`,
  `location`, `profile`, `rental`, `reservation`, `vehicle`).
- **data/** — Retrofit servisleri (`data/remote/`), repository implementasyonları
  (`data/repository/`) ve oturum yönetimi (`data/session/`).
- **presentation/** — Compose ekranları, ortak bileşenler (`component/`), tema ve navigasyon.
  Navigasyon, `presentation/navigation/RenCarNavHost.kt` ve `RenCarDestination.kt` üzerinden tek
  merkezden yönetilir.
- **di/** — Hilt modülleri (`NetworkModule`, `RepositoryModule`).

Mimari ve tasarım kararlarının bağlayıcı referans dokümanları:

- `docs/architecture/mvi-overview.md` — genel prensipler, veri akışı, katman/paket yapısı.
- `docs/architecture/mvi-contracts.md` — State + Intent + Effect kuralları.
- `docs/architecture/mvi-viewmodel-rules.md` — ViewModel, UI bağlama (Route/Screen) ve DI kuralları.
- `docs/decisions.md` — projede alınmış tüm mimari/teknik kararların kronolojik kaydı.

## 5) Proje Yapısı

```
app/src/main/java/com/turkcell/rencar/
    data/
        remote/         // Retrofit API servisleri ve DTO'lar
        repository/     // Repository implementasyonları
        session/        // Oturum/token yönetimi (bellek içi)
    di/                 // Hilt modülleri
    domain/
        auth/  license/  location/  profile/  rental/  reservation/  vehicle/
    presentation/
        component/      // Ortak/yeniden kullanılabilir Compose bileşenleri (ör. harita)
        core/mvi/        // Paylaşılan MVI soyutlamaları
        navigation/      // RenCarDestination, RenCarNavHost
        screen/          // Her biir kendi MVI dosya setine sahip özellik ekranları
        theme/           // Material 3 tema ve renk sistemi
docs/
    api/openapi.json               // Backend API sözleşmesi
    architecture/                  // MVI bağlayıcı dokümanları
    design/00-color-system.md      // Renk sistemi dokümantasyonu
    decisions.md                   // Mimari karar günlüğü
agents.md                          // Proje üzerinde çalışan insan/AI katkı kuralları
```

## 6) Uygulama Akışları ve Ekranlar

Navigasyon rotaları `RenCarDestination.kt` içinde tanımlıdır. Uygulamadaki başlıca ekranlar:

| Ekran (route) | Amaç |
| --- | --- |
| `splash` | Açılış ekranı, oturum durumu kontrolü |
| `login` / `register` / `otp/{phoneNumber}` | Kimlik doğrulama akışı (referans MVI implementasyonu) |
| `license-upload` | Ehliyet ön/arka yüz ve selfie doğrulama akışı |
| `home` | Müsait araçların harita üzerinde gösterimi, konum izni yönetimi, alt navigasyon |
| `car-detail/{vehicleId}` | Seçilen aracın detayı (marka/model, plaka, fiyat, mesafe) |
| `reservation-confirmation/{vehicleId}` | Plan seçimi (dakikalık/saatlik/günlük), sunucu fiyat önizlemesi, rezervasyon onayı |
| `rental-photo-upload/{rentalId}/{vehicleId}/{photoMode}` | Kiralama başlangıcında (4 yön) veya bitişinde araç teslim fotoğrafları |
| `active-rental/{rentalId}/{vehicleId}` | Aktif yolculuk: canlı konum (Socket.IO), anlık ücret/mesafe, süre sayacı |
| `history` | Tamamlanmış kiralamaların aylık gruplanmış geçmişi ve aylık istatistikler |
| `profile` | Kullanıcı profili, ehliyet durumu ve görselleri |

Kullanıcı akışının uçtan uca özeti: **Kayıt/Giriş → Ehliyet Doğrulama → Harita üzerinden araç
keşfi → Araç Detayı → Rezervasyon Onayı ve Plan Seçimi → Teslim Fotoğrafları → Aktif Yolculuk →
Kiralamayı Bitirme → Geçmişte Görüntüleme.**

## 7) Backend Entegrasyonu

- **REST API**: `BuildConfig.API_BASE_URL` üzerinden Retrofit ile erişilir
  (`https://rencarv2.halitkalayci.com/`). API sözleşmesi `docs/api/openapi.json` dosyasında
  tutulur ve istemci tarafında hiçbir alan bu sözleşme dışında uydurulmaz.
- **Live Location**: Aktif kiralamadaki aracın konumu, `Socket.IO` üzerinden `/ws/locations`
  namespace'inin `my-vehicle` event'i dinlenerek alınır (yalnızca CUSTOMER rolü, kendi aktif
  kiralaması kapsamında).
- **Session Management**: Erişim token'ı bellek içi `SessionTokenHolder` ile tutulur; kalıcı/otomatik
  oturum geri yükleme ve reaktif token yenileme bu sürümde bulunmamaktadır.

## 8) Gerekli İzinler

| İzin | Kullanım Amacı |
| --- | --- |
| `INTERNET` | REST API ve Socket.IO bağlantıları |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Harita üzerinde kullanıcı konumunun gösterimi, yakındaki araçların keşfi ve mesafe hesabı |

Kamera erişimi, `android.permission.CAMERA` çalışma zamanı izni istenmeden, sistem kamera
uygulamasına (`ACTION_IMAGE_CAPTURE`) delege edilerek sağlanır; bunun için `FileProvider`
(`res/xml/file_paths.xml`) tanımlanmıştır.

## 9) Kurulum ve Çalıştırma

### Ön Koşullar

- Android Studio (AGP 9.2.1 ve Gradle 9.4.1 ile uyumlu bir sürüm)
- JDK 11
- İnternet erişimi olan bir fiziksel cihaz veya emülatör (canlı konum ve harita servisleri için)

### Adımlar

1. Depoyu klonlayın ve Android Studio ile açın.
2. Gradle senkronizasyonunun tamamlanmasını bekleyin (wrapper üzerinden Gradle 9.4.1 otomatik
   indirilir).
3. `app` modülünü seçili bir cihaz/emülatörde çalıştırın.

Uygulama, derleme zamanında `BuildConfig.API_BASE_URL` alanına gömülü sabit bir backend adresi
kullanır; yerel bir `.env` veya ek yapılandırma adımı gerekmez.

## 10) Test

- Unit Tests: `app/src/test/` (JUnit, kotlinx-coroutines-test, OkHttp MockWebServer ile ağ
  katmanı sahteleme).
- Instrumentation/UI Tests: `app/src/androidTest/` (Espresso, Compose UI Test).

Testleri komut satırından çalıştırmak için:

```
./gradlew test
./gradlew connectedAndroidTest
```

## 11) Dokümantasyon ve Yönetişim

Bu proje üzerinde çalışan her katkıda bulunan (insan veya yapay zekâ), `agents.md` dosyasındaki
kurallara uymakla yükümlüdür. Öne çıkan başlıklar:

- Herhangi bir işlem, birbiriyle ilişkili en fazla 5 dosyalık gruplar hâlinde yürütülür.
- Eksik veya belirsiz bilgi uydurulmaz; böyle bir durumda işlem durdurulup kullanıcıya danışılır.
- Kod üretmeden önce dosya dökümü ve varsa bağımlılık matrisi sunulup onay alınır.
- Mimari/teknik kararlar `docs/decisions.md` içinde kayıt altına alınır.
- Yeni ekranlar MVI mimarisine ve `docs/architecture/` altındaki bağlayıcı dokümanlara uymak
  zorundadır; referans implementasyon Login ekranıdır.

## 12) Bilinen Sınırlamalar

- Oturum token'ı yalnızca bellek içinde tutulur; uygulama tamamen kapatılıp yeniden açıldığında
  otomatik oturum geri yükleme yoktur.
- Reaktif/otomatik token yenileme mekanizması bulunmamaktadır; token süresi dolduğunda Socket.IO
  bağlantısı sessizce kapanır ve ekran yeniden açıldığında taze token ile tekrar bağlanılır.
- Kiralama bitişinde (RETURN_TRIP) seçilen fotoğraflar yalnızca yerelde tutulur; backend
  sözleşmesinde bitiş fotoğrafı yükleyen bir uç nokta bulunmadığından herhangi bir endpoint'e
  gönderilmez.
- Geçmiş kiralama kartlarında GPS rotası/izi gösterilmez; backend bu veriyi sağlamamaktadır.

---

Ayrıntılı karar geçmişi ve gerekçeler için bkz. [`docs/decisions.md`](docs/decisions.md).
