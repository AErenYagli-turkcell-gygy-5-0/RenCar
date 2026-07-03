# MVI Overview

> Bu doküman RenCar sunum katmanında (`presentation/`) kullanılan MVI (Model-View-Intent)
> mimarisinin genel prensiplerini, veri akışını ve paket yapısını tanımlar. `agents.md` §2.4
> gereği bağlayıcıdır; yeni bir ekran/feature bu dokümana uymak zorundadır.

---

## 1) Neden MVI

Sunum katmanında tek yönlü veri akışı (unidirectional data flow) sağlamak ve UI durumunu tek
bir kaynaktan (single source of truth) yönetmek için MVI seçilmiştir. Compose'un deklaratif
doğasıyla doğrudan uyumludur: UI, State'in bir fonksiyonudur.

## 2) Temel Döngü

```
User Action -> Intent -> ViewModel.onIntent() -> State güncellenir / Effect tetiklenir
                                                        |
                                                        v
                                              UI, State'i gözlemleyip yeniden çizilir
                                              UI, Effect'i bir kereliğine tüketir
```

- **State**: Ekranın o anki tam durumunu temsil eden, immutable, tek bir `data class`.
- **Intent**: Kullanıcının (veya sistemin) tetiklediği niyet/olay.
- **Effect**: State'e ait olmayan, bir kereye mahsus yan etki (navigasyon, snackbar, vb.).

Detaylı kurallar için bkz. [mvi-contracts.md](mvi-contracts.md) ve
[mvi-viewmodel-rules.md](mvi-viewmodel-rules.md).

## 3) Katman ve Paket Yapısı

Her ekran/feature kendi paketinde aşağıdaki dosyalarla temsil edilir
(`presentation/screen/<feature>/`):

```
presentation/screen/<feature>/
    <Feature>State.kt      // Ekranın immutable UI state modeli
    <Feature>Intent.kt     // Kullanıcı/sistem olayları
    <Feature>Effect.kt     // Tek seferlik yan etkiler
    <Feature>ViewModel.kt  // MviViewModel<State, Intent, Effect> uygulaması, @HiltViewModel
    <Feature>Screen.kt     // Route (stateful) + Screen (stateless) composable'lar
```

Çekirdek MVI soyutlamaları (marker interface'ler ve taban ViewModel) tüm feature'lar arasında
paylaşılır ve `presentation/core/mvi/` altında tutulur:

```
presentation/core/mvi/
    UiState.kt
    UiIntent.kt
    UiEffect.kt
    MviViewModel.kt
```

## 4) Kapsam Dışı

- Domain/Data katmanları (repository, use case, API) bu dokümanın kapsamında değildir; ayrı bir
  mimari karar olarak ele alınacaktır. Bu doküman yalnızca sunum katmanını (presentation) bağlar.
- ViewModel'lerde şu an gerçek iş mantığı/ağ çağrısı yoktur; ekranlar arası akışı doğrulamak için
  mock/iskelet davranış kullanılır. Gerçek entegrasyon ayrı bir karar ile eklenecektir.

## 5) Referans Implementasyon

Login, Splash ve Otp ekranları (`presentation/screen/auth/login/`,
`presentation/screen/splash/`, `presentation/screen/auth/otp/`) bu dokümanın referans
implementasyonlarıdır. Yeni bir ekran eklerken bu üç ekranın paket/dosya yapısı örnek alınır.
