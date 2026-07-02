# MVI Contracts — State, Intent, Effect Kuralları

> `agents.md` §2.4 gereği bağlayıcıdır. Bu doküman her ekranın `<Feature>Contract.kt`
> dosyasında State/Intent/Effect'in nasıl tanımlanacağını belirler.

---

## 1) Ortak Marker Interface'ler

`presentation/core/mvi/` altında üç marker (boş) interface tanımlıdır:

```kotlin
interface UiState
interface UiIntent
interface UiEffect
```

Her ekranın State/Intent/Effect tipi bu interface'lerden birini implemente etmek zorundadır.
Bunun dışında ortak/genel bir State/Intent/Effect tipi (ör. `BaseState`) tanımlanmaz; her ekran
kendi bağımsız kontratını yazar.

## 2) State Kuralları

- State, **tek bir `data class`** olarak tanımlanır ve `UiState`'i implemente eder.
- State **immutable**'dır; güncelleme her zaman `copy()` ile yeni bir örnek üretilerek yapılır.
- Her alan için ekranın ilk açılış görünümünü yansıtan bir **varsayılan değer** verilir; böylece
  `data class XState(...) : UiState` tek başına `XState()` ile örneklenebilir ve Preview'larda
  kullanılabilir.
- State içinde `Context`, `ViewModel`, Compose `@Composable` çağrısına bağımlı değer (ör.
  `isSystemInDarkTheme()` sonucu) tutulmaz. Tema/görsel kararlar Composable katmanında kalır.
- Yükleme/hata gibi durumlar ayrı ayrı alanlarla temsil edilir (ör. `isLoading: Boolean`,
  `errorMessage: String?`); ayrı bir "Loading/Error/Success" sealed State hiyerarşisi
  kurulmaz (en sade hali için tek data class tercih edilir).

Örnek:

```kotlin
data class LoginState(
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) : UiState
```

## 3) Intent Kuralları

- Intent, `sealed interface` olarak tanımlanır ve `UiIntent`'i implemente eder.
- Her kullanıcı aksiyonu (buton tıklama, metin değişimi vb.) ayrı bir Intent alt tipidir.
- Parametresiz intent'ler için `data object`, parametreli intent'ler için `data class` kullanılır.
- Intent isimlendirmesi geçmiş zaman/olay odaklıdır (ör. `SendCodeClicked`,
  `PhoneNumberChanged`), emir kipi kullanılmaz (`SendCode` değil).

Örnek:

```kotlin
sealed interface LoginIntent : UiIntent {
    data class PhoneNumberChanged(val value: String) : LoginIntent
    data object SendCodeClicked : LoginIntent
}
```

## 4) Effect Kuralları

- Effect, `sealed interface` olarak tanımlanır ve `UiEffect`'i implemente eder.
- Yalnızca **State'e ait olmayan, bir kereye mahsus** olaylar Effect olarak modellenir:
  navigasyon, Snackbar/Toast gösterimi, klavye kapama gibi.
- Ekranın sürekli görünen durumu (ör. hata mesajı metni) Effect değil, State alanıdır.
- Effect'ler `Channel`/`receiveAsFlow()` üzerinden tek seferlik tüketilecek şekilde iletilir
  (bkz. [mvi-viewmodel-rules.md](mvi-viewmodel-rules.md) §2).

Örnek:

```kotlin
sealed interface LoginEffect : UiEffect {
    data class NavigateToOtp(val phoneNumber: String) : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}
```

## 5) Dosya Yerleşimi

Bir ekranın State, Intent ve Effect tanımları **tek bir dosyada** toplanır:
`presentation/screen/<feature>/<Feature>Contract.kt`. Üç tip için ayrı dosya açılmaz.
