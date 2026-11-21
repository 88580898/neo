package com.example.data

data class CurrencyInfo(
    val code: String,
    val nameZh: String,
    val nameEn: String,
    val symbol: String,
    val flag: String,
    val defaultRateFromUsd: Double,
    val fractionDigits: Int = 2,
    val isPopular: Boolean = false
)

/**
 * Built-in catalog of major global currencies with Chinese & English metadata
 * plus realistic baseline USD exchange rates for immediate offline first-launch usability.
 */
object CurrencyCatalog {

    val defaultBoardCodes = listOf("CNY", "USD", "HKD", "JPY", "EUR", "GBP")

    val currencies: List<CurrencyInfo> = listOf(
        CurrencyInfo("CNY", "人民币", "Chinese Yuan", "¥", "🇨🇳", 7.24, 2, isPopular = true),
        CurrencyInfo("USD", "美元", "US Dollar", "$", "🇺🇸", 1.00, 2, isPopular = true),
        CurrencyInfo("HKD", "港币", "Hong Kong Dollar", "HK$", "🇭🇰", 7.78, 2, isPopular = true),
        CurrencyInfo("JPY", "日元", "Japanese Yen", "¥", "🇯🇵", 154.20, 0, isPopular = true),
        CurrencyInfo("EUR", "欧元", "Euro", "€", "🇪🇺", 0.945, 2, isPopular = true),
        CurrencyInfo("GBP", "英镑", "British Pound", "£", "🇬🇧", 0.790, 2, isPopular = true),
        CurrencyInfo("KRW", "韩元", "South Korean Won", "₩", "🇰🇷", 1398.0, 0, isPopular = true),
        CurrencyInfo("TWD", "新台币", "New Taiwan Dollar", "NT$", "🇹🇼", 32.50, 2, isPopular = true),
        CurrencyInfo("SGD", "新加坡元", "Singapore Dollar", "S$", "🇸🇬", 1.342, 2, isPopular = true),
        CurrencyInfo("AUD", "澳大利亚元", "Australian Dollar", "A$", "🇦🇺", 1.538, 2, isPopular = true),
        CurrencyInfo("CAD", "加拿大元", "Canadian Dollar", "C$", "🇨🇦", 1.398, 2, isPopular = true),
        CurrencyInfo("CHF", "瑞士法郎", "Swiss Franc", "CHF", "🇨🇭", 0.885, 2),
        CurrencyInfo("MOP", "澳门元", "Macanese Pataca", "MOP$", "🇲🇴", 8.02, 2),
        CurrencyInfo("THB", "泰铢", "Thai Baht", "฿", "🇹🇭", 34.65, 2),
        CurrencyInfo("MYR", "马来西亚林吉特", "Malaysian Ringgit", "RM", "🇲🇾", 4.46, 2),
        CurrencyInfo("VND", "越南盾", "Vietnamese Dong", "₫", "🇻🇳", 25400.0, 0),
        CurrencyInfo("IDR", "印尼盾", "Indonesian Rupiah", "Rp", "🇮🇩", 15880.0, 0),
        CurrencyInfo("PHP", "菲律宾比索", "Philippine Peso", "₱", "🇵🇭", 58.75, 2),
        CurrencyInfo("INR", "印度卢比", "Indian Rupee", "₹", "🇮🇳", 84.45, 2),
        CurrencyInfo("NZD", "新西兰元", "New Zealand Dollar", "NZ$", "🇳🇿", 1.705, 2),
        CurrencyInfo("RUB", "俄罗斯卢布", "Russian Ruble", "₽", "🇷🇺", 99.50, 2),
        CurrencyInfo("AED", "阿联酋迪拉姆", "UAE Dirham", "د.إ", "🇦🇪", 3.6725, 2),
        CurrencyInfo("SAR", "沙特里亚尔", "Saudi Riyal", "﷼", "🇸🇦", 3.75, 2),
        CurrencyInfo("SEK", "瑞典克朗", "Swedish Krona", "kr", "🇸🇪", 10.95, 2),
        CurrencyInfo("NOK", "挪威克朗", "Norwegian Krone", "kr", "🇳🇴", 11.08, 2),
        CurrencyInfo("DKK", "丹麦克朗", "Danish Krone", "kr", "🇩🇰", 7.05, 2),
        CurrencyInfo("PLN", "波兰兹罗提", "Polish Zloty", "zł", "🇵🇱", 4.08, 2),
        CurrencyInfo("CZK", "捷克克朗", "Czech Koruna", "Kč", "🇨🇿", 23.95, 2),
        CurrencyInfo("HUF", "匈牙利福林", "Hungarian Forint", "Ft", "🇭🇺", 388.0, 0),
        CurrencyInfo("TRY", "土耳其里拉", "Turkish Lira", "₺", "🇹🇷", 34.45, 2),
        CurrencyInfo("ZAR", "南非兰特", "South African Rand", "R", "🇿🇦", 18.12, 2),
        CurrencyInfo("BRL", "巴西雷亚尔", "Brazilian Real", "R$", "🇧🇷", 5.80, 2),
        CurrencyInfo("MXN", "墨西哥比索", "Mexican Peso", "MX$", "🇲🇽", 20.35, 2),
        CurrencyInfo("ARS", "阿根廷比索", "Argentine Peso", "AR$", "🇦🇷", 1002.0, 2),
        CurrencyInfo("CLP", "智利比索", "Chilean Peso", "CLP$", "🇨🇱", 978.0, 0),
        CurrencyInfo("COP", "哥伦比亚比索", "Colombian Peso", "COP$", "🇨🇴", 4410.0, 0),
        CurrencyInfo("ILS", "以色列新谢克尔", "Israeli Shekel", "₪", "🇮🇱", 3.74, 2),
        CurrencyInfo("EGP", "埃及镑", "Egyptian Pound", "E£", "🇪🇬", 49.40, 2),
        CurrencyInfo("PKR", "巴基斯坦卢比", "Pakistani Rupee", "₨", "🇵🇰", 277.8, 2),
        CurrencyInfo("KZT", "哈萨克斯坦坚戈", "Kazakhstani Tenge", "₸", "🇰🇿", 495.0, 2),
        CurrencyInfo("QAR", "卡塔尔里亚尔", "Qatari Riyal", "QR", "🇶🇦", 3.64, 2),
        CurrencyInfo("KWD", "科威特第纳尔", "Kuwaiti Dinar", "KD", "🇰🇼", 0.307, 3),
        CurrencyInfo("BHD", "巴林第纳尔", "Bahraini Dinar", "BD", "🇧🇭", 0.376, 3),
        CurrencyInfo("OMR", "阿曼里亚尔", "Omani Rial", "OMR", "🇴🇲", 0.385, 3),
        CurrencyInfo("ISK", "冰岛克朗", "Icelandic Krona", "kr", "🇮🇸", 138.0, 0)
    )

    private val byCode: Map<String, CurrencyInfo> = currencies.associateBy { it.code }

    fun getOrFallback(code: String): CurrencyInfo {
        val upper = code.uppercase()
        return byCode[upper] ?: CurrencyInfo(
            code = upper,
            nameZh = upper,
            nameEn = upper,
            symbol = upper,
            flag = "🌐",
            defaultRateFromUsd = 1.0,
            fractionDigits = 2
        )
    }

    fun matchesQuery(info: CurrencyInfo, query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        return info.code.lowercase().contains(q) ||
            info.nameZh.lowercase().contains(q) ||
            info.nameEn.lowercase().contains(q) ||
            info.symbol.lowercase().contains(q)
    }
}
