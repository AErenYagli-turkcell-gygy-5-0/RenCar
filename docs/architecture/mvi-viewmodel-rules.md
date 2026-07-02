# MVI ViewModel, UI Bağlama ve DI Kuralları

> `agents.md` §2.4 gereği bağlayıcıdır. Bu doküman ViewModel'lerin nasıl yazılacağını,
> Composable katmanına nasıl bağlanacağını (Route/Screen ayrımı) ve DI (Hilt) kullanımını
> belirler.

---

## 1) Taban ViewModel

Tüm ekran ViewModel'leri `presentation/core/mvi/MviViewModel.kt` içindeki taban sınıfı extend
eder:

```kotlin
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected fun setState(reduce: S.() -> S) {
        _state.update(reduce)
    }

    protected fun sendEffect(build: () -> E) {
        viewModelScope.launch { _effect.send(build()) }
    }

    abstract fun onIntent(intent: I)
}
```

- `setState` dışında `_state` doğrudan dışarıdan değiştirilmez.
- `onIntent`, ViewModel'e giren **tek giriş noktasıdır**; Composable katmanı State'i asla
  doğrudan mutasyona uğratmaz, yalnızca `onIntent(...)` çağırır.

## 2) Ekran ViewModel'i

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    // ileride eklenecek use case/repository bağımlılıkları buraya enjekte edilir
) : MviViewModel<LoginState, LoginIntent, LoginEffect>(LoginState()) {

    override fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged ->
                setState { copy(phoneNumber = intent.value) }
            LoginIntent.SendCodeClicked -> handleSendCode()
        }
    }

    private fun handleSendCode() {
        sendEffect { LoginEffect.NavigateToOtp(state.value.phoneNumber) }
    }
}
```

- Her ekran ViewModel'i `@HiltViewModel` ile işaretlenir ve bağımlılıklarını `@Inject
  constructor` üzerinden alır.
- ViewModel içinde Compose/Context/Android framework bağımlılığı (ör. `Context`,
  `isSystemInDarkTheme()`) bulunmaz.

## 3) UI Bağlama — Route / Screen Ayrımı

Her ekran, aynı dosyada iki composable'a bölünür:

- **`<Feature>Route`** (stateful): `hiltViewModel()` ile ViewModel'i alır, `state`'i
  `collectAsStateWithLifecycle()` ile toplar, `effect` akışını `LaunchedEffect(Unit)` içinde
  dinleyip navigasyon gibi bir kerelik aksiyonları tetikler. `<Feature>Screen`'i çağırır.
- **`<Feature>Screen`** (stateless): Mevcut ekranlardaki gibi saf UI'dır; `state: <Feature>State`
  ve `onIntent: (<Feature>Intent) -> Unit` parametre olarak alınır. `@Preview` fonksiyonları
  sabit `State` örnekleriyle bu composable'ı çağırmaya devam eder — ViewModel/Hilt'e bağımlılık
  yoktur.

```kotlin
@Composable
fun LoginRoute(
    onNavigateToOtp: (String) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToOtp -> onNavigateToOtp(effect.phoneNumber)
                is LoginEffect.ShowError -> Unit // ör. Snackbar
            }
        }
    }

    LoginScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun LoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    // mevcut UI, artık state'ten okur ve aksiyonlarda onIntent(...) çağırır
}
```

- `LaunchedEffect(Unit)` anahtarı sabit tutulur; `state` gibi değişken bir anahtar kullanılması
  her state değişiminde effect toplayıcının yeniden başlamasına ve effect kaçırma/çift
  tetiklenme riskine yol açar.

## 4) DI (Hilt) Kuralları

- Uygulama sınıfı `@HiltAndroidApp` ile işaretlenir (`RenCarApplication`) ve
  `AndroidManifest.xml`'de `android:name` olarak kayıtlıdır.
- `MainActivity`, `@AndroidEntryPoint` ile işaretlenir.
- Ekran ViewModel'leri Composable içinde `viewModel()` ile değil, **`hiltViewModel()`** ile elde
  edilir (`androidx.hilt:hilt-navigation-compose`).
- Şu an domain/data katmanı bulunmadığından `@Module`/`@Provides` gerektiren bir bağımlılık
  yoktur; ilk modüller, use case/repository katmanı eklendiğinde ayrı bir karar ile
  tanımlanacaktır.

## 5) Navigasyon

Ekranlar arası geçiş `presentation/navigation/RenCarNavHost.kt` üzerinden, `NavHost` +
`composable(route) { XxxRoute(...) }` kalıbıyla yönetilir. Route'lar arası parametre geçişi
(ör. telefon numarası) `NavController` route argümanları ile yapılır; `Screen` composable'ları
navigasyondan habersizdir, yalnızca `Route` katmanı `NavController` bilir.
