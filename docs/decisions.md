# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

# Mimari Kararlar

---

## 2026-07-16 - Kiralamayı Bitirme Onayı, Ödeme (Cüzdan/Kart) ve Cüzdan Ekranının Eklenmesi

**Karar (bitirme onayı):** Aktif Kiralama ekranındaki "Kiralamayı Bitir" butonu artık doğrudan foto
akışına gitmez; önce ekran-özel bir `AlertDialog` (`ProfileScreen`'deki `LogoutConfirmationDialog`
ile aynı desen) gösterilir. `ActiveRentalIntent.FinishClicked` yalnızca
`ActiveRentalState.showFinishConfirmDialog`'ı açar; mevcut `handleFinishClicked()` mantığı (job
iptalleri + `NavigateToFinishPhotoUpload` effect'i) yeni `FinishConfirmed` intent'ine taşınmıştır.
Foto akışı ve `POST /rentals/:id/finish` çağrısı (2026-07-14 kararındaki RETURN_TRIP yerel-kapı
davranışı) hiç değişmemiştir.

**Karar (Ödeme / Kiralama Özeti ekranı — yeni):** `RentalPhotoUploadViewModel.finishTrip()` artık
başarı sonrası `NavigateHome` yerine yeni `NavigateToPayment(rentalId)` effect'ini gönderir.
`presentation/screen/payment/` altında MVI ile yeni bir ekran eklenmiştir; ekran yalnızca
`rentalId` nav argümanıyla açılır ve kendi verisini `GET /rentals/{id}` (yeni `getOne` ucu) ile
tazeler — 2026-07-11 kararındaki "ID taşı, tazele" ilkesiyle tutarlıdır. Ücret dökümü backend'in
`finish` formülüyle (`totalPrice = usageFee + startFee + serviceFee`) birebir aynı şekilde
`usageFee = totalPrice - startFee - serviceFee` olarak türetilir; hiçbir kalem istemcide farklı
hesaplanmaz.

**Karar (Cüzdan/Kart ödeme seçimi):** Ekranda yalnızca "kart değiştir" değil, Cüzdan/Kart segment
kontrolü sunulur. Cüzdan bakiyesi (`GET /wallet`) ve kayıtlı kartlar (`GET /cards`) paralel
yüklenir; varsayılan seçim, bakiye tutarı karşılıyorsa Cüzdan, karşılamıyorsa (ve varsayılan bir
kart varsa) Kart olacak şekilde belirlenir. Ödeme `POST /rentals/:id/pay` (`method=WALLET` veya
`method=CARD`+`cardId`) ile alınır. Cüzdan bakiyesi yetersizse (`walletBalance < netAmount`) "Bakiye
yüklemek ister misiniz?" onay diyaloğu gösterilir; "Evet" Cüzdan ekranına yönlendirir (otomatik
tekrar deneme yoktur, kullanıcı yükleme sonrası ödemeyi elle tekrar başlatır).

**Karar (Cüzdan / Ödeme Yöntemleri ekranı — yeni):** `presentation/screen/wallet/` altında yeni bir
MVI ekranı eklenmiştir: bakiye + "Bakiye Yükle" (`POST /wallet/topup`, 10-5000 TL doğrulaması
istemci tarafında da uygulanır), kayıtlı kartlar + "+ Ekle" (`POST /cards` — yalnızca marka + son 4
hane + SKT; tam kart numarası/CVV alanı yoktur, backend zaten reddeder), son 20 işlem
(`WalletTransaction` listesi) ve varsayılan olmayan her kartta "Varsayılan yap" aksiyonu
(`PATCH /cards/{id}/default`). Ekran, mevcut dört sekmeli `BottomNavBar`'ı (Harita/Geçmiş/Cüzdan/
Profil) Home/History ekranlarıyla aynı şekilde barındırır; navbar'daki "Cüzdan" sekmesi artık
`BottomNavItem.Wallet` durumunda `Unit` yerine gerçek yönlendirme yapar (`HomeViewModel`,
`HistoryViewModel` effect üzerinden; `ProfileScreen` doğrudan callback üzerinden — mevcut ekranların
kendi desenleri korunmuştur).

**Karar (API katmanı genişletmesi):** `data/remote/wallet/` ve `data/remote/cards/` altında yeni
Retrofit servisleri + DTO'lar eklendi (`docs/api/openapi.json`'daki mevcut `/wallet`, `/wallet/topup`,
`/cards`, `/cards/{id}/default` uçlarına birebir karşılık gelir). `RentalApiService`'e `GET
/rentals/{id}` ve `POST /rentals/{id}/pay` eklendi; istemcinin sadeleştirilmiş
`RentalResponseDto.kt`'si backend'in gerçek (zengin) `RentalResponseDto` şemasıyla eşleşecek şekilde
genişletildi (`vehicle`, `startedAt`, `startFee`, `serviceFee`, `distanceKm`, `durationMinutes`,
`paymentStatus`, `paymentMethod`, `discountAmount`) — bu alanlar backend'de zaten dönüyordu, hiçbir
alan uydurulmadı (bkz. `docs/api/openapi.json` satır 3235-3375).

**Kapsam dışı (icat edilmedi):**
- Kart silme (`DELETE /cards/{id}`) — backend'de mevcut ancak ne kullanıcı talebinde ne mockup'ta
  istenmediği için eklenmedi.
- İndirim kodu giriş alanı — talep edilmedi; `discountAmount` yalnızca backend `pay` yanıtında sıfırdan
  farklı dönerse gösterilir, ödeme öncesi girdi alanı yoktur.
- Bakiye yükleme sonrası ödemeye otomatik dönüş/tekrar deneme — kullanıcı Cüzdan ekranından manuel
  geri dönüp ödemeyi tekrar başlatır.

**Bağımlılıklar:** Yeni bir Gradle bağımlılığı eklenmemiştir; Retrofit/Gson/Hilt mevcut düzenle
aynen kullanılmıştır.

**Etkilenen alanlar:**
- `Rencar.html` (Aktif Kiralama onay popup'ı, Ödeme ekranı Cüzdan/Kart seçici, Cüzdan ekranı
  "Varsayılan yap")
- `data/remote/wallet/`, `data/remote/cards/` (yeni)
- `domain/wallet/`, `domain/cards/` (yeni)
- `data/repository/wallet/`, `data/repository/cards/` (yeni)
- `data/remote/rental/dto/RentalResponseDto.kt`, yeni `PayRentalRequestDto.kt`/`PayRentalResponseDto.kt`,
  `data/remote/rental/RentalApiService.kt`, `domain/rental/Rental.kt`, yeni
  `PaymentStatus.kt`/`PaymentMethod.kt`/`PaymentReceipt.kt`, `domain/rental/RentalRepository.kt`,
  `data/repository/rental/ApiRentalRepository.kt`
- `presentation/screen/rental/active/` (`ActiveRentalState.kt`, `ActiveRentalIntent.kt`,
  `ActiveRentalViewModel.kt`, `ActiveRentalScreen.kt`)
- `presentation/screen/rental/photo/` (`RentalPhotoUploadEffect.kt`, `RentalPhotoUploadViewModel.kt`,
  `RentalPhotoUploadScreen.kt`)
- `presentation/screen/payment/` (yeni), `presentation/screen/wallet/` (yeni)
- `presentation/navigation/RenCarDestination.kt`, `presentation/navigation/RenCarNavHost.kt`
- `presentation/screen/home/` (`HomeEffect.kt`, `HomeViewModel.kt`, `HomeScreen.kt`)
- `presentation/screen/history/` (`HistoryEffect.kt`, `HistoryViewModel.kt`, `HistoryScreen.kt`)
- `presentation/screen/profile/ProfileScreen.kt`
- `di/NetworkModule.kt`, `di/RepositoryModule.kt`
- `app/src/main/res/values/strings.xml`

---

## 2026-07-16 - Kiralama Geçmişi Görsel Hiyerarşisinin Güçlendirilmesi

**Karar:** Geçmiş ekranının domain, repository ve MVI sözleşmeleri değiştirilmeden sunum katmanı
yenilendi. Mevcut aylık istatistikler iki ayrı özet kartında gösterildi; tamamlanmış kiralamalar
`startedAt` alanıyla aylara göre gruplandı. Kartlara mevcut araç tipi rengiyle araç ikonu, plaka,
tamamlanma rozeti, ince tema kenarlığı ve uzun araç adları için taşma kontrolü eklendi. Boş ve hata
durumları görsel hiyerarşi ve belirgin yeniden deneme aksiyonuyla iyileştirildi.

Geçmiş rota koordinatları API tarafından sağlanmadığından mini rota veya harita görseli üretilmedi.
Yeni renk rolü ya da bağımlılık eklenmedi; mevcut Material 3 ve `RenCarExtendedColors` kullanıldı.

**Etkilenen dosyalar:**
- `presentation/screen/history/HistoryScreen.kt`
- `app/src/main/res/values/strings.xml`
- `docs/decisions.md`

---

## 2026-07-15 - Ehliyet Yuklemede Selfie Multipart Alaninin API Sozlesmesine Eklenmesi

**Karar:** Guncel `docs/api/openapi.json` sozlesmesine gore `POST /license/upload` artik
`front`, `back` ve `selfie` multipart alanlarini zorunlu kabul eder. Bu nedenle ehliyet dogrulama
akisi, Selfie adiminda `TakePicturePreview()` ile uretilen JPEG byte dizisini `selfie` alan adiyla
backend'e gonderir. `front` ve `back` alan adlari korunur.

**Gerekce:** Onceki kararlarda backend'in selfie alani saglamadigi kabul edilmisti; guncel OpenAPI
bu varsayimi degistirmistir. Selfie'nin yalnizca lokal profil fotografi olarak tutulmasi yeni
backend sozlesmesiyle eksik multipart istegi olusturur ve `400` hata cevabina yol acabilir.

**Kapsam disi:** Register akisi bu kararda degistirilmedi. `POST /auth/register` zorunlu request
alanlari mevcut istemciyle uyumludur; opsiyonel `referralCode` ve register sonrasi token/OTP urun
karari ayri kapsamda degerlendirilecektir.

**Etkilenen alanlar:**
- `domain/license/`, `data/remote/license/`, `data/repository/license/`
- `presentation/screen/auth/license/`
- `presentation/screen/profile/` test fake'leri

---

## 2026-07-14 - Aktif Kiralama Ekranı Yeniden Tasarımı, Harita Takibi ve Ana Sayfa Bildirim Çipi

**Karar (ekran yeniden tasarımı):** Kullanıcının paylaştığı ekran görüntüsüne (`img.png`) göre
`ActiveRentalScreen.kt` yeniden düzenlendi: üstte geri oku + "Aktif Yolculuk" başlığı, araç bilgi
kartı (marka/model, plaka, plan etiketi — Dakikalık/Saatlik/Günlük), kart şeklinde (tam ekran
olmayan, 220dp yükseklikte, yuvarlak köşeli) harita, "Geçen süre" kartı (artık başlangıç saatiyle
birlikte), Anlık Ücret/Mesafe kartları, başlangıç ücreti bilgi notu ve alt butonlar. Eski
`StatusPill` (üstteki "Kiralama aktif · X" rozeti) kaldırıldı; aynı metin artık yalnızca Ana
Sayfa'daki yeni bildirim çipinde kullanılıyor (`active_rental_status_active_with_vehicle` string'i
yeniden kullanıldı, `active_rental_status_active` (araçsız varyant) artık kullanılmadığından
kaldırıldı).

**Karar (Anlık Ücret incelemesi — hata değil, eksik açıklama):** `GET /rentals/active` yanıtındaki
`currentCost` alanı sözleşme gereği zaten "kullanım + açılış ücreti + servis ücreti" toplamıdır
(`openapi.json`, `ActiveRentalResponseDto.currentCost` açıklaması); istemci bunu birebir doğru
gösteriyordu, bir hesaplama hatası tespit edilmedi. Ancak ekranda bu tutarın neden bir taban
değerden (açılış ücretinden) başladığını açıklayan bilgi hiç yoktu. Backend'in zaten döndürdüğü ama
istemcinin sadeleştirilmiş DTO'sunda eksik olan `startFee`, `plan`, `startedAt` alanları
(`ActiveRentalResponseDto.kt`, `domain/rental/ActiveRental.kt`, `ApiRentalRepository.kt` içine)
eklendi ve ekrana **kullanıcı onaylı** bir bilgi notu kondu: "Anlık ücrete ₺{başlangıç ücreti}
başlangıç ücreti dahildir; kesin tutar kiralama bitince hesaplanır." Mockup'taki "...bitince çıkar"
ifadesi kullanılmadı çünkü `finish` formülünde (`kullanım + açılış ücreti + servis ücreti`) açılış
ücreti bitişte tutardan düşülmüyor, toplamın kalıcı bir parçası olarak kalıyor; yanlış mali çağrışım
kurulmaması için kullanıcıyla netleştirilip bu şekilde onaylandı.

**Karar (saniye sayacı):** `ActiveRentalViewModel`'e, mevcut 5 saniyelik REST polling'in (ücret/
mesafe için) yanına bağımsız bir `tickerJob` eklendi: her saniye `elapsedSeconds + 1` yapılır; her
poll yanıtı geldiğinde `elapsedSeconds` sunucu değeriyle üzerine yazılmaya devam ettiğinden sapma
birikmez. Ücret/mesafe hâlâ yalnızca 5 saniyede bir tazelenir (bunlar sunucu tarafı hesap
gerektirir; saniyede bir ağ isteği atmak israf olurdu) — kullanıcının talebi yalnızca süre
sayacının akıcı ilerlemesiydi.

**Karar (harita: araç ikonu + sürekli merkezleme):** `RencarMap.kt`'ye, mevcut `myLocation` (mavi
nokta, Ana Sayfa'daki "benim konumum" göstergesi — **değiştirilmedi**) parametresinden bağımsız
yeni bir `vehicleLocation: LatLng?` parametresi eklendi. `SOURCE_ME`/`LAYER_ME` ile birebir aynı
desende ayrı bir `GeoJsonSource` + `SymbolLayer` kullanılır; ikon, projede zaten var olan (Splash'te
kullanılan) `res/drawable/ic_rencar_car.xml` beyaza boyanıp mavi dairesel arka plan üzerine
çizilerek üretilir (`createPriceBubbleBitmap` ile aynı Canvas/Bitmap deseni) — yeni bir görsel
varlık eklenmedi. `ActiveRentalScreen.kt`, konum her güncellendiğinde (Ana Sayfa'daki "yalnız ilk
seferde ortala" deseninin aksine) `mapController.animateTo(location)` çağırarak kamerayı aracın
üstünde tutar; mavi ikon ekranda sabit kalır.

**Karar (geri tuşu ve Ana Sayfa bildirim çipi — kullanıcı onayı, önceki kararın kısmi revizyonu):**
Üstteki geri oku artık her zaman Ana Sayfa'ya döner (`ActiveRentalEffect.NavigateToHome` →
`RenCarNavHost`'ta `navigate(Home) { popUpTo(Home, inclusive=true) }`); kiralama arka planda ACTIVE
kalmaya devam eder (ViewModel job'ları iptal edilmez sadece ekran değişir; yeniden girildiğinde
`start()` zaten kaldığı yerden devam ettirir). Bu, 2026-07-14 tarihli "Aktif Kiralaması Olan
Kullanıcının da Kaldığı Yerden Devam Etmesi" kararını **yalnızca ACTIVE durumu için** revize eder:
`HomeViewModel.checkActiveRental()`'ın ACTIVE dalı artık otomatik yönlendirme yerine
`HomeState.activeRentalId/activeRentalVehicleId/activeRentalVehicleName` alanlarını doldurup Ana
Sayfa'yı normal şekilde göstermeye devam eder; Ana Sayfa'da `HomeActiveRentalBanner` (mevcut
`active_rental_status_active_with_vehicle` metniyle) gösterilir, dokununca **mevcut**
`HomeEffect.NavigateToActiveRentalScreen` effect'i tetiklenir. **PREPARING dalı değişmedi** — foto
akışı tamamlanmadan kiralama başlamadığından Ana Sayfa'da gezinmenin bir anlamı yok, otomatik
yönlendirme aynen korundu.

**Bağımlılıklar:** Yeni bir Gradle bağımlılığı eklenmedi; mevcut `ic_rencar_car.xml` yeniden
kullanıldı.

**Etkilenen alanlar:**
- `data/remote/rental/dto/ActiveRentalResponseDto.kt`, `domain/rental/ActiveRental.kt`,
  `data/repository/rental/ApiRentalRepository.kt`
- `presentation/screen/rental/active/` (tüm dosyalar)
- `presentation/component/map/RencarMap.kt`
- `presentation/navigation/RenCarNavHost.kt`
- `presentation/screen/home/` (`HomeState.kt`, `HomeIntent.kt`, `HomeViewModel.kt`,
  `HomeScreenComponents.kt`, `HomeScreen.kt`)
- `app/src/main/res/values/strings.xml`

---

## 2026-07-14 - Aktif Kiralama Ekranında Canlı Konum (Socket.IO) ve Kiralama Geçmişi Ekranı

**Karar (canlı konum):** Kullanıcının paylaştığı backend sözleşmesi doğrultusunda (`Socket.IO`
namespace `/ws/locations`, `my-vehicle` event'i — yalnız CUSTOMER'ın aktif kiralamasındaki aracın
konumu, `{ ts, vehicle: { vehicleId, latitude, longitude, ... } }`) yeni bir `domain/location/`
sınırı eklendi (`VehicleLocation.kt`, `LocationRepository.kt`). `data/remote/location/
LocationSocketClient.kt`, `io.socket:socket.io-client` kütüphanesiyle bu namespace'e
`SessionTokenHolder.accessToken`'ı `auth.token` olarak göndererek bağlanır ve yalnızca
`"my-vehicle"` event'ini `callbackFlow` ile `Flow<VehicleLocation>`'a çevirir. `vehicle-positions`
event'i (yalnız ADMIN, tüm filo) kapsam dışıdır — uygulamada admin arayüzü yok.

`ActiveRentalViewModel`, mevcut `pollingJob` (REST `GET /rentals/active`) deseninin yanına ikinci,
bağımsız bir `locationJob` ekler; ikisi ayrı kaynaklardan beslenir (biri süre/ücret için REST
polling, diğeri konum için socket) ve `onCleared()`'da birlikte iptal edilir.

**Karar (harita gösterimi — kullanıcı onayı):** `RencarMap.kt` bileşeni hiç değiştirilmedi.
Aracın canlı konumu, bileşenin zaten sahip olduğu iki gösterim türünden **`myLocation` (mavi
"konumum" noktası)** ile gösterilir; fiyat balonu (`vehicles`) parametresi kullanılmaz. Bu,
2026-07-14 tarihli "Kiralama Başlatma..." kararında kapsam dışı bırakılan "Aktif Kiralama
ekranında canlı harita/rota gösterimi" maddesini tamamlar; mockup'taki özel araç pin'i ve
kesikli rota çizgisi icat edilmedi (agents.md §2.2).

**Bilinen sınırlama (token yenileme):** Projede reaktif/otomatik token yenileme mekanizması hiç
yok (bkz. 2026-07-13 kararındaki "Kapsam sınırı" — `AuthInterceptor` yalnızca header ekler).
Bu nedenle socket bağlantısında da `connect_error` durumunda yeniden deneme/refresh akışı
eklenmedi; token süresi dolarsa bağlantı sessizce kapanır, kullanıcı ekranı yeniden açtığında
(`ScreenStarted`) taze token ile tekrar bağlanılır. Yeni bağımlılık: `io.socket:socket.io-client:2.1.1`.

**Karar (Kiralama Geçmişi ekranı):** `Rencar.html` "09 Kiralama Geçmişi" mockup'ına dayanan yeni
`presentation/screen/history/` ekranı eklendi; ana ekrandaki daha önce hiçbir rotaya bağlı olmayan
"Geçmiş" alt navigasyon sekmesi (`BottomNavItem.History`) artık bu ekrana yönlendiriyor
(`HomeViewModel.NavItemSelected` → `HomeEffect.NavigateToHistory`).

`docs/api/openapi.json`'daki güncel `RentalResponseDto` şemasının, uygulamanın o an kullandığı
sadeleştirilmiş DTO'dan (`data/remote/rental/dto/RentalResponseDto.kt`) çok daha zengin olduğu
tespit edildi: `vehicle` (plate/brand/model/type özeti), `distanceKm`, `durationMinutes`,
`totalPrice`, `status`, `startedAt` alanlarını içeriyor. Bu, Geçmiş ekranının ihtiyaç duyduğu her
şeyi karşıladığından hiçbir alan uydurulmadı. `HomeViewModel`'in aktif kiralama kontrolü için
kullandığı mevcut `getMyRentals()`/`RentalSummaryResponseDto` (yalnız id/vehicleId/status)
dokunulmadan bırakıldı; aynı `GET /rentals` uç noktasına, Geçmiş ekranı için ayrı ve daha zengin
bir DTO ile ikinci bir Retrofit metodu (`listMineDetailed()`) eklendi. `GET /rentals/stats`
(`RentalRepository.getRentalStats()`) ayın özetini ("Bu ay N yolculuk · ₺X harcama" başlığı) besler.
`RentalRepository.getRentalHistory()` yalnızca `status == COMPLETED` kayıtları döndürür (kullanıcı
isteği: bir kiralama bitirildiğinde kaydı Geçmiş'te tutulsun).

**Karar (kart görseli — kullanıcı onayı):** Mockup'taki dekoratif "mini rota haritası" eskizi
uydurulmadı — backend geçmiş bir yolculuğun GPS rotasını/izini döndürmüyor, yalnızca toplam
`distanceKm`/`durationMinutes` sayısal olarak dönüyor. Bunun yerine mevcut `VehicleType.color()`
palet mantığıyla (RencarMap'teki aynı kategori renkleri) renklendirilmiş basit bir ikon kullanıldı.

**Etkilenen alanlar:**
- `domain/location/`, `data/remote/location/`, `data/repository/location/` (yeni)
- `domain/rental/` (`RentalHistoryItem.kt`, `RentalStats.kt` yeni; `RentalRepository.kt` genişletildi)
- `data/remote/rental/` (yeni DTO'lar, `RentalApiService.kt`), `data/repository/rental/ApiRentalRepository.kt`
- `presentation/screen/history/` (yeni)
- `presentation/screen/rental/active/` (`ActiveRentalState.kt`, `ActiveRentalViewModel.kt`, `ActiveRentalScreen.kt`)
- `presentation/screen/home/` (`HomeEffect.kt`, `HomeViewModel.kt`, `HomeScreen.kt`)
- `presentation/navigation/RenCarDestination.kt`, `presentation/navigation/RenCarNavHost.kt`
- `di/RepositoryModule.kt`
- `gradle/libs.versions.toml`, `app/build.gradle.kts` (yeni bağımlılık: `io.socket:socket.io-client`)
- `app/src/main/res/values/strings.xml`

---

## 2026-07-14 - Aktif Kiralaması Olan Kullanıcının da Kaldığı Yerden Devam Etmesi

**Karar:** 2026-07-13 kararı yalnızca "aktif rezervasyon" (`GET /reservations/active`) durumunu
kapsıyordu; ancak rezervasyon bir kiralamaya dönüştüğünde (`POST /rentals`) CONVERTED olarak
işaretlendiğinden artık "aktif rezervasyon" olarak görünmez. Bu nedenle kullanıcı foto yükleme
(PREPARING) veya süren yolculuk (ACTIVE) aşamasındayken uygulamayı kapatıp tekrar açarsa Home
önceki haliyle bunu yakalayamıyor, normal harita/araç listesini gösteriyordu.

`HomeViewModel`'e `RentalRepository` enjekte edildi. `checkActiveReservation()` "aktif
rezervasyon yok" (404) sonucunu artık doğrudan `loadVehicles()`'e değil, yeni
`checkActiveRental()` fonksiyonuna yönlendirir. Bu fonksiyon `GET /rentals` (`getMyRentals()`)
ile PREPARING/ACTIVE durumundaki bir kiralama arar (`CarDetailViewModel.loadCanUnlock()` ile
aynı desen/eşik kümesi — `RESUMABLE_RENTAL_STATUSES`); bulunursa PREPARING için Araç Teslim
Fotoğrafı (START_TRIP), ACTIVE için Aktif Kiralama ekranına yönlendirilir ve Home back stack'ten
çıkarılır (`popUpTo(Home, inclusive = true)`), araç listesi hiç yüklenmez. Bulunamazsa veya
`getMyRentals()` hata dönerse (network vb.) sessizce `loadVehicles()`'e düşülür — bu, aynı
`CarDetailViewModel.loadCanUnlock()`'taki "hata durumunda sessizce pasif kal" ilkesiyle
tutarlıdır; ikincil bir kontrolün ağ hatası tüm Home ekranını kilitlememelidir.

**Gerekçe:** Kullanıcı talebi doğrultusunda, PREPARING/ACTIVE kontrolü rezervasyon kontrolüyle
aynı önceliğe (uygulamaya yeniden girişte, araç listesinden önce) alınmıştır. Rezervasyon ve
kiralama durumları backend'de birbirini dışladığından (rezervasyon dönüşünce CONVERTED olur) iki
kontrol sıralı ve tek seferlik yapılmıştır; paralel/tekrarlayan bir polling eklenmemiştir.

**Bilinen sınırlama:** Bu akış için otomatik test eklenmemiştir (projede hâlihazırda
`HomeViewModelTest.kt` yoktu); yalnızca derleme ve mevcut test paketiyle doğrulanmıştır.

**Etkilenen alanlar:**
- `presentation/screen/home/` (`HomeState.kt`, `HomeEffect.kt`, `HomeViewModel.kt`, `HomeScreen.kt`)
- `presentation/navigation/RenCarNavHost.kt`

---

## 2026-07-14 - Araç Teslim Fotoğrafı Ekranına Kamera Seçeneği Eklenmesi

**Karar:** Araç Teslim Fotoğrafı ekranındaki (`presentation/screen/rental/photo/`) her foto
karesine dokunulduğunda artık yalnızca galeri değil, "Kameradan çek / Galeriden seç" seçenekli
bir diyalog gösterilir. Kamera seçeneği `ActivityResultContracts.TakePicture()` ile
uygulanmıştır (License ekranındaki selfie akışında kullanılan `TakePicturePreview()`'dan farklı
olarak tam çözünürlüklü dosya üretir — "Hasarları net çek" uyarısı düşük çözünürlüklü önizleme
ile çelişeceğinden bilinçli olarak farklı bir contract seçilmiştir). `TakePicture()` sonucu bir
hedef `Uri` gerektirdiğinden, projeye ilk kez bir `FileProvider` eklenmiştir
(`AndroidManifest.xml` içine `<provider>` tanımı + yeni `res/xml/file_paths.xml`,
`cache-path="rental_photos/"`). Geçici dosyalar uygulamanın `cacheDir` altında oluşturulur;
kalıcı depolama veya harici depolama izni gerekmez.

**Gerekçe:** `android.permission.CAMERA` çalışma zamanı izni EKLENMEMİŞTİR — `TakePicture()`
implicit `ACTION_IMAGE_CAPTURE` intent'i ile sistem kamera uygulamasına delege eder, izin
sistem kamera uygulaması tarafından yönetilir. Yeni bir Gradle bağımlılığı gerekmemiştir
(`FileProvider` `androidx.core` içinde, proje zaten `core-ktx` kullanıyor).

**Etkilenen alanlar:**
- `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/file_paths.xml` (yeni)
- `presentation/screen/rental/photo/RentalPhotoUploadScreen.kt`
- `app/src/main/res/values/strings.xml`

---

## 2026-07-14 - Kiralama Başlatma (Foto Akışı) ve Kiralama Bitirme Ekranlarının Eklenmesi

**Karar:** `Rencar.html` içindeki "Araç Teslim Fotoğrafı · 4 yön" ekranı (adım 12) incelendi;
alt butonu "Kiralamayı Başlat · N foto kaldı" olduğundan bu ekranın backend'deki
`POST /rentals` (plan PER_MINUTE/HOURLY → PREPARING) → `POST /rentals/:id/photos` (4 yön) →
`POST /rentals/:id/start` akışına karşılık geldiği tespit edildi. Bu akış için istemciye
`RentalApiService`/`ApiRentalRepository` üzerinden `uploadPhoto`, `getPhotos`, `start`,
`getActive`, `finish`, `cancel` uçları eklendi (`domain/rental/RentalPhotoSide.kt`,
`RentalPhotosState.kt`, `ActiveRental.kt` yeni domain modelleri ile). Rezervasyon onayı
tamamlandığında (`ReservationConfirmationEffect.ReservationCreated` artık `vehicleId` ve
backend'in döndürdüğü `status`'tan türetilen `isPreparing` alanlarını da taşır) plan
PER_MINUTE/HOURLY ise yeni `presentation/screen/rental/photo/` (Araç Teslim Fotoğrafı) ekranına,
plan DAILY ise (backend'de foto adımı olmadığından, anında ACTIVE döndüğü için) doğrudan yeni
`presentation/screen/rental/active/` (Aktif Kiralama) ekranına yönlendirilir.

**Karar (bitiş fotoğrafı — kullanıcı onayı):** Aktif Kiralama ekranındaki "Kiralamayı Bitir"
butonu da aynı 4-foto ekranını (`RentalPhotoUploadMode.RETURN_TRIP`) kullanır; ancak OpenAPI
sözleşmesinde yolculuk BİTİŞİNDE fotoğraf zorunluluğunu destekleyen bir endpoint yoktur —
`POST /rentals/:id/photos` yalnızca PREPARING aşamasında çalışır (ACTIVE'de 409), `POST
/rentals/:id/finish` hiçbir gövde/fotoğraf almaz. Bu nedenle RETURN_TRIP modunda seçilen 4
fotoğraf yalnızca yerelde (`Uri`) tutulur, hiçbir endpoint'e yüklenmez — yalnızca yerel bir
zorunluluk kapısı olarak davranır; 4/4 seçilince asıl bitirme `POST /rentals/:id/finish` ile
yapılır. Bu, `agents.md` §2.2 gereği var olmayan bir endpoint icat etmemek için kullanıcı ile
netleştirilerek onaylanmış bir tasarım kararıdır (backend'de gerçek bir bitiş-fotoğrafı
endpoint'i eklenirse RETURN_TRIP modu ileride kolayca gerçek yüklemeye çevrilebilir; ViewModel
zaten mod bazlı ayrıştırılmıştır).

**Karar (CarDetail "Kilidi Aç" bağlama):** `CarDetailScreen.kt` içinde önceden boş bırakılan
(`onClick = {}`) "Kilidi Aç" butonu `CarDetailIntent.UnlockClicked`'e bağlandı.
`CarDetailViewModel.loadCanUnlock()` artık yalnızca `canUnlock: Boolean` değil, eşleşen
kiralamanın `unlockRentalId`/`unlockRentalStatus` (PREPARING/ACTIVE) bilgisini de state'e yazar;
`UnlockClicked` bu duruma göre foto ekranına (PREPARING) veya Aktif Kiralama ekranına (ACTIVE)
yönlendirir.

**Kapsam dışı (icat edilmedi):**
- Mockup'taki "Kilitle/Aç" butonunun fiziksel kilit işlevi — backend'de karşılığı olmadığından
  Aktif Kiralama ekranında dekoratif/pasif bırakıldı (2026-07-10 kararındaki aynı ilke: API'de
  karşılığı olmayan davranış icat edilmez).
- Aktif Kiralama ekranında canlı harita/rota gösterimi — mockup'ta var ama bu kararın kapsamı
  dışında tutuldu, sade bir kart düzeni kullanıldı.
- `POST /rentals/:id/pay` ödeme akışı (finish sonrası) — bu kararın kapsamı yalnızca foto
  zorunluluğu ve bitirme çağrısını kapsar.
- Rezervasyon onayı ekranındaki plan seçimi/quote mantığına dokunulmadı (önceki bir karar/PR ile
  zaten eklenmişti); yalnızca "rezervasyon tamamlandı" sonrası yönlendirme genişletildi.

**Bağımlılıklar:** Yeni bir Gradle bağımlılığı eklenmemiştir. Fotoğraf önizlemesi mevcut
`ApiLicenseRepository`/`LicenseUploadScreen` deseniyle (`ContentResolver` + `BitmapFactory` +
`Image(bitmap=...)`) yapılmıştır; proje Coil kullanmadığından yeni bir görsel yükleme
kütüphanesi eklenmemiştir.

**Etkilenen alanlar:**
- `domain/rental/`, `data/remote/rental/`, `data/repository/rental/`
- `presentation/screen/rental/photo/` (yeni), `presentation/screen/rental/active/` (yeni)
- `presentation/screen/reservation/confirmation/`
- `presentation/screen/cardetail/`
- `presentation/navigation/RenCarDestination.kt`, `presentation/navigation/RenCarNavHost.kt`
- `app/src/main/res/values/strings.xml`

---

## 2026-07-14 - Mobil Uygulamanin RenCar V2 API Adresine Tasinmasi

**Karar:** Android uygulamasinin `BuildConfig.API_BASE_URL` degeri
`https://rencarv2.halitkalayci.com/` olarak guncellenmistir. Swagger arayuzunun `/api/docs`
altinda yayinlanmasi API endpoint'lerinin `/api/` on eki kullandigi anlamina gelmez; canli
`/health` endpoint'i 200, `/api/health` endpoint'i 404 dondugu icin Retrofit base URL kok alan
adi olarak belirlenmistir.

**Gerekce:** Eski `https://rencar.halitkalayci.com/` sunucusu guncel arac fiyat, yakit,
sanziman ve koltuk alanlarini ve `/vehicles/{id}/quote` endpoint'ini sunmadigi icin istemci yeni
alanlari sifir olarak gosteriyor ve fiyat onizlemesinde 404 aliyordu.

**Bagimliliklar:** Yeni bagimlilik eklenmemistir.

**Etkilenen dosya:**
- `app/build.gradle.kts`

---

## 2026-07-14 - Rezervasyon Onayinda Plan Secimi ve Sunucu Fiyat Onizlemesi

**Karar:** Rezervasyon onay ekrani arac detayini `GET /vehicles/{id}` ile yuklemeye devam eder;
dakikalik, saatlik ve gunluk plan fiyatlarini bu yanittan gosterir. Secili planin tahmini ucret
dokumu istemcide hesaplanmaz, `GET /vehicles/{id}/quote` ucundan 30 dakikalik kullanim icin alinir.
Plan degistiginde quote yeniden yuklenir. Kiralama olusturulurken secili plan `POST /rentals`
govdesine eklenir; `endDate` yalnizca `DAILY` planda gonderilir.

**Gerekce:** Acilis ve servis ucreti dahil fiyat formulleri backend tarafindan yonetilmektedir.
Sunucu quote sonucu, onay ekraninda gosterilen tahmini tutar ile yolculuk sonunda uygulanacak
fiyatlandirmanin ayni kurallara dayanmasini saglar.

### 2026-07-16 - Plan Bazli Tahmini Ucret Aciklamasi

**Karar:** Rezervasyon onay ekraninda quote yanitinin `usageFee`, `startFee`, `serviceFee` ve
`estimatedTotal` kalemleri ayri satirlarda gosterilir. Tahmini toplam etiketi `PER_MINUTE` icin
secilen dakika, `HOURLY` icin "Ilk saat", `DAILY` icin "1 gun" olarak plan bazinda aciklanir.
Istemci fiyat hesaplamaz; sunucunun dondurdugu kalemleri ve toplami aynen gosterir.

**Gerekce:** Saatlik ve gunluk planlarda 30 dakikalik quote sorgusu sirasiyla en az bir saate ve
en az bir gune yuvarlanir. Tum planlarda "30 dk" etiketi kullanilmasi, toplam tutarin orantili
yarim saat ucreti oldugu izlenimini vermektedir. Plan bazli etiket ve fiyat dokumu, backend
fiyatlandirma kurallarini degistirmeden toplam tutari kullanici icin aciklar.

**Bagimliliklar:** Yeni bagimlilik eklenmemistir.

**Etkilenen dosyalar:**
- `app/src/main/java/com/turkcell/rencar/presentation/screen/reservation/confirmation/ReservationConfirmationState.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/reservation/confirmation/ReservationConfirmationViewModel.kt`
- `app/src/main/java/com/turkcell/rencar/presentation/screen/reservation/confirmation/ReservationConfirmationScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/test/java/com/turkcell/rencar/presentation/screen/reservation/confirmation/ReservationConfirmationViewModelTest.kt`
## 2026-07-13 - Aktif Rezervasyonlu Kullanicinin Arac Detayindan Baslamasi

**Karar:** Home ekrani acilirken arac listesi yuklenmeden once `GET /reservations/active` ile
kullanicinin aktif rezervasyonu kontrol edilir. Endpoint aktif rezervasyon dondururse Home haritasi
ve diger arac marker'lari yuklenmez; kullanici aktif rezervasyondaki `vehicleId` ile CarDetail
ekranina yonlendirilir. Bu yonlendirme aktif rezervasyon kaynakli oldugu icin Home route'u back
stack'ten cikarilir.

**Aktif rezervasyon yok durumu:** `GET /reservations/active` icin yalnizca `404` cevabi "aktif
rezervasyon yok" kabul edilir ve mevcut Home harita akisi normal sekilde devam eder. `401`, `403`,
network veya beklenmeyen hatalarda harita akisina dusulmez; kullanici retry durumunda tutulur.

**Arac detay kaynagi:** OpenAPI sozlesmesinde `GET /vehicles/{id}` musait olmayan araci yalnizca
aktif kiralamasi olan kullanici icin gorunur tarif ettigi icin aktif rezervasyonlu RESERVED aracta
`404` alma riski vardir. Bu durumda CarDetail, `GET /reservations/active` cevabindaki `vehicle`
ozetini fallback kaynak olarak kullanir. Bu ozette bulunmayan saatlik/gunluk fiyat, yakit, menzil,
vites, koltuk ve segment alanlari ekranda varsayilan degerlerle gosterilmez.

**Kapsam siniri:** Uygulamada token hala bellek-ici `SessionTokenHolder` ile tutulur. Process tamamen
kapanip tekrar acildiginda otomatik oturum geri yukleme bu karar kapsaminda eklenmemistir; bunun
icin ayri bir kalici session/token karari gerekir.

**Bagimliliklar:** Yeni bagimlilik eklenmemistir.

**Etkilenen alanlar:**
- `domain/vehicle/`, `data/remote/vehicle/`, `data/repository/vehicle/`
- `domain/rental/`, `data/remote/rental/`, `data/repository/rental/`
- `presentation/screen/reservation/confirmation/`
- `data/remote/reservation/`, `data/repository/reservation/`, `domain/reservation/`
- `presentation/screen/home/`
- `presentation/screen/cardetail/`
- `presentation/navigation/RenCarNavHost.kt`

---

## 2026-07-13 - Profil Ekraninda Backend Ehliyet Gorsellerinin Gosterilmesi

**Karar:** Profil ekranindaki ehliyet durum karti, `GET /license/status` cevabindan gelen
`frontImageUrl` ve `backImageUrl` alanlarini kullanarak kullanicinin yukledigi ehliyetin on ve arka
yuz gorsellerini modal icinde gosterir. Gorsel URL'leri yoksa uydurma veya lokal fallback gorsel
kullanilmaz; modal icinde bos durum bilgilendirmesi gosterilir.

**Kapsam:** Bu karar yalnizca profil ekranindaki goruntuleme davranisini kapsar. `POST
/license/upload` isteginin mevcut `front` ve `back` multipart gonderimi degistirilmemistir; yeni
backend sozlesmesindeki `selfie` alani bu is kapsaminda istemci upload akisina eklenmemistir.

**Bagimliliklar:** Yeni bagimlilik eklenmemistir. Backend URL'lerinden gorsel gostermek icin
mevcut Android `BitmapFactory` ve `java.net.URL` API'leri kullanilir.

**Etkilenen alanlar:**
- `presentation/screen/profile/`

---

## 2026-07-11 — İlk Kayıt Selfie'sinin Lokal Profil Fotoğrafı Olarak Kullanılması

**Karar:** `docs/api/openapi.json` içinde `POST /license/upload` yalnızca `front` ve `back`
multipart alanlarını kabul eder; `GET /auth/me` yanıtındaki `UserResponseDto` içinde profil
fotoğrafı, avatar veya selfie URL alanı yoktur. Bu nedenle ilk kayıt/ehliyet akışında çekilen
selfie backend'e gönderilmez; ehliyet yükleme başarılı olduktan sonra kullanıcının `id` değeriyle
eşleşen JPEG dosyası uygulamanın private `filesDir/profile_photos/` alanında tutulur.

**Gerekçe:** Backend sözleşmesinde selfie/profil fotoğrafı kontratı bulunmadığından API alanı
uydurulmamıştır. Issue kapsamındaki "backend veya local cache" notu doğrultusunda, profil ekranı
`GET /auth/me` ile gelen kullanıcı id'si üzerinden lokal cache'teki selfie'yi profil fotoğrafı
olarak gösterecektir. Cache bulunamazsa mevcut varsayılan profil görseli kullanılmaya devam eder.

**Etkilenen alanlar:**
- `domain/profile/`, `data/repository/profile/`, `di/RepositoryModule.kt`
- `presentation/screen/auth/license/`
- `presentation/screen/profile/`

---

## 2026-07-11 — Araç Detaydan Rezervasyon Onayına Vehicle ID ile Navigasyon

**Karar:** Araç Detay ekranındaki “Rezerve Et” eylemi MVI akışıyla
`CarDetailIntent.ReserveClicked` intent'ine, ardından seçilen `vehicleId` değerini taşıyan
`CarDetailEffect.NavigateToReservationConfirmation` effect'ine bağlanmıştır. NavHost içinde
`reservation-confirmation/{vehicleId}` rotası tanımlanmış ve Rezervasyon Onayı ekranına yalnızca
araç kimliği aktarılmıştır.

**Gerekçe:** Araç Detay state'inde bulunan marka, model, plaka ve fiyat değerleri navigasyon
argümanlarında tekrar edilmemiştir. Rezervasyon Onayı ekranı mevcut `GET /vehicles/{id}` çağrısıyla
aracın güncel müsaitlik ve fiyat bilgisini yeniden yükler. Böylece iki ekran arasında eski veya
tutarsız araç verisi taşınmaz.

**Back stack davranışı:** Rezervasyon Onayı ekranındaki geri eylemi Araç Detay ekranına döner.
`POST /rentals` başarılı olduğunda Home ekranına dönülür ve Araç Detay ile Rezervasyon Onayı
ekranları back stack'ten çıkarılır.

**Bağımlılıklar:** Yeni bağımlılık eklenmemiştir.

**Etkilenen alanlar:**
- `presentation/screen/cardetail/`
- `presentation/navigation/RenCarDestination.kt`
- `presentation/navigation/RenCarNavHost.kt`
- `presentation/screen/reservation/confirmation/` (mevcut route yeniden kullanıldı)

---

## 2026-07-10 — Rezervasyon Onayı: Araç Detayı ve Kiralama API Entegrasyonu

**Karar:** Rezervasyon onayı ekranı, sunum katmanındaki bağlayıcı MVI kurallarına uygun olarak
`State`, `Intent`, `Effect`, `ViewModel` ve `Screen` dosyalarına ayrılmıştır. Ekran `vehicleId` ile
başlatılır, `GET /vehicles/{id}` üzerinden aracın plaka, marka, model, tip ve günlük fiyat bilgisini
yükler. Kullanıcı koşulları onayladıktan sonra `POST /rentals` isteği gönderilir; başarılı sonuç
`ReservationCreated(rentalId)` effect'i ile ekranın çağırıcısına aktarılır.

**Kiralama planı kararı:** OpenAPI sözleşmesinde yalnızca `pricePerDay` bulunduğu için dakikalık ve
saatlik planlar eklenmemiştir. Ekranda tek gerçek plan olarak günlük kiralama gösterilir ve API'nin
zorunlu `endDate` alanı, istek anından bir gün sonrası UTC ISO-8601 değeri olarak oluşturulur. Yakıt,
vites, koltuk, ücretsiz rezervasyon süresi ve başlangıç ücreti gibi OpenAPI'de bulunmayan tasarım
alanları eklenmemiştir.

**Hata yönetimi:** `400`, `401`, `403`, `404` ve `409` cevapları domain hata türlerine çevrilir.
OpenAPI, `409` cevabını hem aracın müsait olmaması hem de kullanıcının aktif kiralaması bulunması
için kullandığından istemci bu iki durumu uydurma bir ayrımla göstermeyip ortak rezervasyon
çakışması mesajı sunar.

**Navigasyon kapsamı:** Kullanıcı talebi doğrultusunda `RenCarDestination` ve `RenCarNavHost`
değiştirilmemiştir. Ekran yalnızca `onNavigateBack` ve `onReservationCreated` callback'lerini sunar;
çağıran ekranın bağlantısı araç detay ekranı tamamlandığında ayrıca yapılacaktır.

**Bağımlılıklar:** Yeni bir Gradle bağımlılığı eklenmemiştir.

**Etkilenen alanlar:**
- `domain/vehicle/`, `data/remote/vehicle/`, `data/repository/vehicle/`
- `domain/rental/`, `data/remote/rental/`, `data/repository/rental/`
- `presentation/screen/reservation/confirmation/`
- `di/NetworkModule.kt`, `di/RepositoryModule.kt`
## 2026-07-10 — CarDetailScreen: Haritadan Araç Seçimi, Konum İzni Kısıtlaması ve API'de Karşılığı Olmayan Alanların Kaldırılması

**Karar (yeni ekran):** `presentation/screen/cardetail/` altında, mevcut MVI dosya yapısıyla
(`CarDetailState.kt`, `CarDetailIntent.kt`, `CarDetailEffect.kt`, `CarDetailViewModel.kt`,
`CarDetailScreen.kt`) yeni bir `CarDetailScreen` eklenmiştir. Kullanıcı Ana Harita'da bir araç
marker'ına tıkladığında `GET /vehicles/{id}` ile araç detayı çekilip bu ekranda gösterilir.

**Karar (haritada araca tıklama):** `RencarMap.kt` içindeki `SymbolManager`'a
`OnSymbolClickListener` eklenmiş; her araç marker'ı `SymbolOptions.withData(JsonPrimitive(vehicle.id))`
ile araç id'sini taşır. `RencarMap` artık `onVehicleClick: (String) -> Unit` parametresi alır.
`HomeIntent.VehicleMarkerClicked` → `HomeViewModel` → `HomeEffect.NavigateToCarDetail` →
`HomeRoute`/`RenCarNavHost` zinciriyle `CarDetail` route'una (araç id'si + kullanıcının son bilinen
konumu query argüman olarak) geçilir.

**Karar (konum izni kısıtlaması):** 2026-07-09 kararındaki `HomeState.permissionDenied` guard
deseni yeniden kullanılmıştır: `HomeViewModel.onIntent`'te `VehicleMarkerClicked`,
`permissionDenied == false` olmadığı sürece hiçbir effect göndermez. Böylece konum izni
verilmeden kullanıcı haritayı gezebilir ve araçları görebilir, ancak bir araca tıklayarak
CarDetail ekranına geçemez. Yeni bir izin/depolama mekanizması eklenmemiştir.

**Karar (API'de karşılığı olmayan alanların kaldırılması — kullanıcı onayı):** Tasarım kaynağındaki
(`Rencar.html`, "04 Araç Detay") Yakıt %, Menzil (km), Vites ve Koltuk sayısı kartları ile araç
fotoğrafı tamamen kaldırılmıştır; `VehicleResponseDto`'da bu alanların karşılığı yoktur ve
`agents.md` §2.2 gereği uydurulmamıştır (kullanıcı onayı ile). Gösterilen alanlar yalnızca
backend'de karşılığı olanlarla sınırlıdır: marka/model, plaka, durum rozeti ("MÜSAİT" — `/vehicles/{id}`
yalnızca AVAILABLE araç döndürdüğünden sabit metin olarak bırakılmıştır, ayrı bir `VehicleStatus`
enum'ı eklenmemiştir) ve `pricePerDay`.

**Karar (fiyat gösterimi — kullanıcı onayı):** Tasarımdaki dakikalık/saatlik fiyat satırları,
backend'in tek sağladığı `pricePerDay` üzerinden matematiksel olarak türetilerek korunmuştur
(`pricePerDay / 1440` dakikalık, `pricePerDay / 24` saatlik). Bu, backend'in kastetmediği bir
kiralama modelinin varsayılması anlamına gelir; kullanıcı bu türetmeyi açıkça onaylamıştır.

**Karar (mesafe metni):** "~250 m uzaklığında" tarzı metin uydurulmamış; `HomeState.myLocation`
(kullanıcının son bilinen konumu) ile aracın gerçek `latitude`/`longitude` değerleri arasında
İstemci tarafında Haversine formülüyle hesaplanmıştır (`CarDetailScreen.kt` içinde özel/private
fonksiyon). Kullanıcının konumu bilinmiyorsa (`myLatitude`/`myLongitude` null) mesafe satırı hiç
gösterilmez.

**Karar (CTA butonları — kullanıcı onayı):** "Rezerve Et" ve "Kilidi Aç" butonları bu iterasyonda
pasiftir (tıklanınca hiçbir şey olmaz); Profil ekranındaki pasif menü satırları örüntüsüyle
tutarlıdır (bkz. 2026-07-07 kararı). Rezervasyon/kilit açma akışı (`Rencar.html`'deki ayrı "05
Rezervasyon Onayı" ekranı) bu kararın kapsamı dışındadır, ayrı bir karar gerektirir.

**Karar (arka plan haritası — kullanıcı onayı):** Bottom sheet'in arkasında statik/dekoratif bir
görsel yerine mevcut `RencarMap` bileşeni yeniden kullanılmış, kamera aracın konumuna
ortalanmıştır (`myLocation = null` verilerek kullanıcı konum noktası bu ekranda gösterilmez,
karışıklığı önlemek için). Yeni bir bağımlılık eklenmemiştir.

**Karar (sürükleyerek kapatma):** Compose Material3 `BottomSheetScaffold` +
`rememberStandardBottomSheetState(skipHiddenState = false)` kullanılmıştır. Sheet tamamen aşağı
çekilip `SheetValue.Hidden` durumuna geçtiğinde `CarDetailIntent.BackClicked` → `CarDetailEffect
.NavigateBack` → `NavController.popBackStack()` tetiklenir; sol üstteki geri oku da aynı intent'i
tetikler. Yeni bir kütüphane gerekmemiştir (proje zaten Compose Material3 BOM 2026.02.01
kullanıyordu).

**Bilinen sınırlama — ikon:** Tasarımdaki "Kilidi Aç" butonundaki kilit ikonu eklenmemiştir;
projede mevcut ikonlar yalnızca `res/drawable` altında elle eklenmiş XML vector drawable'lar
olarak tutulduğundan (bkz. 2026-07-06 kararı) ve yeni bir ikon varlığı eklemek bu kararın onaylanan
dosya kapsamının dışında kaldığından buton yalnızca metin olarak bırakılmıştır. İkon eklenmesi
istenirse ayrı bir onay ile `ic_lock.xml` eklenebilir.

**Gerekçe:**
- `agents.md` §2.2 gereği backend'de karşılığı olmayan veri (yakıt, menzil, vites, koltuk, araç
  fotoğrafı) uydurulamaz; kullanıcıya seçenekler sunulmuş ve kaldırma yönünde onay alınmıştır.
- Konum izni kısıtlaması, 2026-07-09 kararındaki "harita dışı ekranlara geçiş konum izni
  gerektirir" ilkesinin CarDetail'e de genişletilmiş halidir; ayrı bir mekanizma icat edilmemiştir.
- `Vehicle` domain modeline `plate`/`brand`/`model` eklenmesi, `VehicleResponseDto`'da zaten var
  olan ama önceden maplenmeyen alanların kullanılması anlamına gelir; uydurma değildir.

**Etkilenen alanlar:**
- `domain/vehicle/` (`Vehicle.kt`, `VehicleError.kt`, `VehicleRepository.kt`)
- `data/remote/vehicle/VehicleApiService.kt`, `data/repository/vehicle/ApiVehicleRepository.kt`
- `presentation/component/map/RencarMap.kt`
- `presentation/screen/home/` (`HomeIntent.kt`, `HomeEffect.kt`, `HomeViewModel.kt`, `HomeScreen.kt`)
- `presentation/screen/cardetail/` (yeni)
- `presentation/navigation/RenCarDestination.kt`, `presentation/navigation/RenCarNavHost.kt`
- `app/src/main/res/values/strings.xml`

---

## 2026-07-09 — Konum İzni Zorunluluğu: Ekran Geçişinin Kısıtlanması, Kalıcı Red Durumunda Ayarlar Yönlendirmesi ve Son Bilinen Konumla Hızlı Zoom

**Karar (navigasyon kısıtlaması):** `HomeViewModel.onIntent`'teki `NavItemSelected` işleyicisine
guard eklenmiştir: `HomeState.permissionDenied` `false` (yani izin açıkça verilmiş) olmadığı sürece
`HomeEffect.NavigateToProfile` gönderilmez ve `selectedNavItem` değişmez. Böylece konum izni
verilmeden kullanıcı Harita (Home) ekranından Profil'e (veya ileride eklenecek diğer sekmelere)
geçemez.

**Karar (kalıcı red / "İzin Ver" butonunun çalışmaması sorunu):** `HomeIntent.LocationPermissionResult`
`granted: Boolean` alanının yanına `canRequestAgain: Boolean` eklenmiştir. `HomeRoute`, izin sonucu
geldiğinde `ActivityCompat.shouldShowRequestPermissionRationale` ile bu değeri hesaplar
(`androidx.activity.compose.LocalActivity` üzerinden alınan `Activity` ile). İzin kalıcı
reddedildiğinde (`canRequestAgain == false`) Android işletim sistemi `ActivityResultContracts
.RequestMultiplePermissions().launch(...)` çağrısında artık hiçbir sistem diyaloğu göstermez; bu
nedenle "İzin Ver" tıklaması ve Home ekranı her açıldığında otomatik tetiklenen istek (`HomeScreen
.kt` içindeki `LaunchedEffect(Unit)`), bu durumda `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`
ile uygulamanın sistem ayarlarına yönlendirir. Ayrıca bu durumda, Home ekranı her kompoze
olduğunda (uygulama her açıldığında) kapatılamaz nitelikte olmayan ama varsayılan olarak açık gelen
bir `AlertDialog` (`LocationPermissionSettingsDialog`) gösterilir; kullanıcı "Kapat" ile geçici
olarak kapatabilir, ancak izin durumu değişmediği sürece bir sonraki uygulama açılışında diyalog
tekrar görünür.

**Karar (hızlı zoom):** Konum izni verildiğinde artık yalnızca canlı `requestLocationUpdates`
sonucu beklenmez; aynı `DisposableEffect` içinde `FusedLocationProviderClient.lastLocation` ile
cihazdaki önbellekteki son bilinen konum da sorgulanır ve varsa hemen `HomeIntent.MyLocationChanged`
ile state'e yazılır. `HomeScreen` içindeki mevcut "ilk konumda bir kez zoom yap" mantığı
(`hasAnimatedToUser`) değiştirilmemiştir; sadece konumun state'e ulaşma hızı iyileştirilmiştir.

**Karar (Ayarlar'dan dönüşte izin senkronizasyonu — ek düzeltme):** Kullanıcı `openAppSettings()` ile
sistem Ayarlar'ına yönlendirilip izni oradan verdiğinde, `permissionLauncher`'ın sonuç callback'i
hiç tetiklenmiyordu (çünkü izin isteği sistem izin diyaloğu üzerinden değil, Ayarlar ekranı üzerinden
veriliyor); bu nedenle uygulamaya geri dönüldüğünde `HomeState` güncel kalmıyor, harita/navigasyon
kısıtlaması izin verilmiş olmasına rağmen kaldırılmıyordu. `HomeRoute`'a, `presentation/component
/map/RencarMap.kt` içinde zaten kullanılan `LocalLifecycleOwner` + `LifecycleEventObserver`
deseniyle bir `ON_RESUME` dinleyicisi eklendi: ekran her ön plana döndüğünde
`ContextCompat.checkSelfPermission` ile izin yeniden kontrol edilir; izin verilmiş ama state hâlâ
"reddedildi" ise `HomeIntent.LocationPermissionResult(granted = true, canRequestAgain = true)`
gönderilerek state senkronize edilir.

**Not — 2026-07-07 kararıyla ilişki:** Bu karar, aşağıdaki "2026-07-07 — Ana Ekran Haritası" kararında
belirtilen "izin reddedildiğinde harita varsayılan merkezle gösterime devam eder, kilitlenmez"
ilkesini BOZMAMAKTADIR: harita içeriği (varsayılan merkez, banner) aynı şekilde görünmeye devam
eder. Bu karar yalnızca uygulama içi diğer ekranlara (şu an için Profil) geçişi kısıtlamaktadır.

**Gerekçe:**
- Kullanıcı talebi: konum izni verilmeden harita dışındaki ekranlara geçilmemesi ve izin
  alınana kadar uygulama her açıldığında iznin tekrar istenmesi.
- `shouldShowRequestPermissionRationale` tabanlı ayrım, Android'in resmi/standart kalıcı-red tespit
  yöntemidir; ek bir kalıcı depolama (SharedPreferences/DataStore) gerektirmez, bu nedenle yeni bir
  bağımlılık eklenmemiştir.
- `lastLocation`, Play Services Location API'sinin standart parçasıdır (ek bağımlılık gerekmez);
  canlı `requestLocationUpdates` akışı GPS soğuk başlangıçta gecikebildiğinden açılışta zoom'u
  hızlandırmak için eklenmiştir.

**Etkilenen alanlar:**
- `presentation/screen/home/` (`HomeState.kt`, `HomeIntent.kt`, `HomeViewModel.kt`, `HomeScreen.kt`)
- `app/src/main/res/values/strings.xml`

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
- Ehliyet yükleme kutuları seçilen `frontImageUri`/`backImageUri` değerlerini UI katmanında
  `ContentResolver` ile yerel bitmap önizlemesine çevirir ve görseli kırpmadan tam görünür
  şekilde gösterir. Görsel yüklüyken kutu sabit yükseklik yerine bitmap en-boy oranını kullanır;
  bu yüzden fotoğraf dar bir alana sıkıştırılmaz. Bu yalnızca görsel geri bildirimdir;
  `LicenseUploadState`, ViewModel akışı ve `/license/upload` API sözleşmesi değişmez.

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
