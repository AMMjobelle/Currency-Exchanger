# Currency Exchange App 💱

A modern Android application for real-time currency exchange with live rates and balance management.

## 📱 Features

- **Real-Time Exchange Rates**: Automatically fetches and updates currency exchange rates every 5
  seconds
- **Multi-Currency Support**: Support for multiple currencies (EUR, USD, GBP, JPY, and more)
- **Balance Management**: Track balances across multiple currencies
- **Instant Conversion**: Real-time calculation of exchange amounts
- **Network Detection**: Automatic detection of internet connectivity with user-friendly dialogs
- **Modern UI**: Clean, card-based Material Design 3 interface
- **Horizontal Scrolling**: View all currency balances with smooth horizontal scrolling
- **Input Validation**: Ensures valid amounts and sufficient balance before exchange

## 🎨 User Interface

### Main Screen

- **Currency Exchange Header**: Title with network status indicator
- **My Balances**: Horizontally scrollable balance cards showing all currencies
- **Exchange Section**:
    - SELL card (red icon with ↑) - Input amount and select currency to sell
    - RECEIVE card (green icon with ↓) - View calculated amount to receive
- **Live Rate Display**: Shows current exchange rate between selected currencies
- **Submit Button**: Process the currency exchange

### Dialogs

- **Success Dialog**: Confirmation of completed exchange
- **Network Dialog**: Alert when internet connection is unavailable with retry option

## 🛠️ Technologies Used

### Android Development

- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern declarative UI framework
- **Material Design 3**: Latest Material Design components
- **Android SDK 31+**: Target SDK 36
-**Firebender AI

### Architecture & Libraries

- **MVVM Architecture**: Clean separation of concerns
- **Kotlin Coroutines**: Asynchronous programming
- **Flow & StateFlow**: Reactive state management
- **ViewModel**: Lifecycle-aware state management
- **Retrofit**: HTTP client for API calls
- **Moshi**: JSON serialization/deserialization
- **OkHttp**: HTTP client with logging interceptor

### Testing

- **JUnit**: Unit testing framework
- **Kotlin Test**: Testing utilities
- Comprehensive test coverage for:
    - Domain use cases
    - Repository implementations
    - ViewModel logic
    - Data models
    - Network connectivity

## 📋 Requirements

- **Minimum SDK**: Android 12 (API 31)
- **Target SDK**: Android 14 (API 36)
- **Compile SDK**: Android 14 (API 36)
- **Kotlin Version**: 2.0.21
- **Gradle**: 8.7.2
- **Internet Permission**: Required for fetching exchange rates

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or later
- Android SDK with API 31+

### Installation

1. **Clone the repository**

2. **Open in Android Studio**
    - Open Android Studio
    - Select "Open an existing project"
    - Navigate to the cloned directory
    - Wait for Gradle sync to complete

3. **Run the app**
    - Connect an Android device or start an emulator
    - Click the "Run" button or press

## 🏗️ Project Structure

```
app/src/main/java/com/jsbanez/currencyexchanger/
├── MainActivity.kt                      # Main UI and Compose screens
├── ExchangeViewModel.kt                 # ViewModel with business logic
├── ExchangeViewModelFactory.kt          # Factory for ViewModel injection
├── NetworkConnectivityManager.kt        # Network state monitoring
├── CurrencyExchangeApi.kt              # Retrofit API interface
├── DomainModels.kt                     # Data models (RatesDomain, BalancesDomain)
├── DomainRepositories.kt               # Repository interfaces
├── dataRepositories.kt                 # Repository implementations
└── DomainUseCases.kt                   # Business logic use cases
    ├── GetRatesUseCase                 # Fetch exchange rates
    ├── ConvertAmountUseCase            # Currency conversion logic
    └── PerformExchangeUseCase          # Exchange execution

app/src/test/java/com/jsbanez/currencyexchanger/
├── BalancesRepositoryImplTest.kt       # Repository tests
├── ConvertAmountUseCaseTest.kt         # Conversion logic tests
├── DomainModelsTest.kt                 # Data model tests
├── DomainUseCasesTest.kt               # Use case tests
├── ExchangeViewModelTest.kt            # ViewModel tests
├── ExchangeViewModelFormattingTest.kt  # UI formatting tests
├── NetworkConnectivityManagerTest.kt   # Network tests
└── RatesRepositoryImplTest.kt          # API repository tests
```

## 🔧 Configuration

### API Configuration

The app uses a public exchange rates API. The configuration is in `CurrencyExchangeApi.kt`:

```kotlin
private const val BASE_URL = "https://developers.paysera.com/tasks/api/"
```

### Initial Balance

Default starting balance is set in `BalancesRepositoryImpl.kt`:

```kotlin
private var balances = BalancesDomain(values = mapOf("EUR" to 1000.0))
```

### Rate Refresh Interval

Exchange rates are refreshed every 15 seconds (configurable in `ExchangeViewModel.kt`):

```kotlin
delay(15_000) // refresh every 15 seconds
```

## 🧪 Testing

Run the unit tests:

```bash
./gradlew test
```

Run specific test classes:

```bash
./gradlew test --tests "*.ConvertAmountUseCaseTest"
./gradlew test --tests "*.ExchangeViewModelTest"
```

### Test Coverage

- ✅ Currency conversion logic with edge cases
- ✅ Balance management and validation
- ✅ Exchange operations and constraints
- ✅ Repository data mapping
- ✅ ViewModel state management
- ✅ Network connectivity detection
- ✅ Input validation and formatting

## 📱 Usage

1. **View Balances**: Scroll horizontally to see all your currency balances
2. **Select Currencies**:
    - Tap the currency dropdown in the SELL card to choose what to sell
    - Tap the currency dropdown in the RECEIVE card to choose what to receive
3. **Enter Amount**: Type the amount you want to sell
4. **Check Rate**: View the live exchange rate and calculated receive amount
5. **Submit**: Tap the SUBMIT button to complete the exchange
6. **Confirmation**: View the success dialog showing the transaction details

## ⚙️ Features in Detail

### Network Connectivity

- Automatic detection of network status
- Visual warning indicator when offline
- Dialog prompt with retry option when network is unavailable
- Graceful handling of network errors

### Balance Management

- Initial balance of 1000 EUR
- Real-time balance updates after exchanges
- Prevention of negative balances
- Support for multiple currencies
- Persistent balance across app sessions (in-memory)

### Exchange Logic

- Accurate cross-currency conversion
- Base currency conversion (EUR)
- Quote currency conversion
- Validation of sufficient funds
- Prevention of same-currency exchanges
- Prevention of zero or negative amount exchanges

## 🎨 Design Features

### Color Scheme

- **Background**: Light blue-grey (#E8EEF2)
- **Cards**: Pure white (#FFFFFF)
- **Primary Button**: Bright blue (#2196F3)
- **Sell Icon**: Red (#E74C3C)
- **Receive Icon**: Green (#2ECC71)
- **Text Primary**: Dark grey (#2C3E50)
- **Text Secondary**: Mid grey (#5D6D7E)

### Typography

- Compact font sizes for efficient space usage
- Clear hierarchy with proper weight and size variations
- 13-22sp range for optimal readability

## 🐛 Known Limitations

- Balances are stored in-memory and reset on app restart
- Network connectivity is required for accurate exchange rates
- Limited to currencies provided by the API
- No transaction history feature
- No offline exchange rate caching

## 👨‍💻 Developer

**JSBanez**
---

**Built with ❤️ using Kotlin and Jetpack Compose**
