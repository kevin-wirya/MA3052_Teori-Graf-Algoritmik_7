export interface GeoLocation {
  name: string;
  subdivision?: string;
  lat: number;
  lon: number;
}

export interface CountryData {
  name: string;
  code: string;
  locations: GeoLocation[];
}

export const worldCities: GeoLocation[] = [
  { name: "Jakarta", subdivision: "Indonesia", lat: -6.2088, lon: 106.8456 },
  { name: "Tokyo", subdivision: "Japan", lat: 35.6762, lon: 139.6503 },
  { name: "New York", subdivision: "United States", lat: 40.7128, lon: -74.006 },
  { name: "London", subdivision: "United Kingdom", lat: 51.5074, lon: -0.1278 },
  { name: "Paris", subdivision: "France", lat: 48.8566, lon: 2.3522 },
  { name: "Beijing", subdivision: "China", lat: 39.9042, lon: 116.4074 },
  { name: "New Delhi", subdivision: "India", lat: 28.6139, lon: 77.209 },
  { name: "Moscow", subdivision: "Russia", lat: 55.7558, lon: 37.6173 },
  { name: "Brasília", subdivision: "Brazil", lat: -15.7938, lon: -47.8828 },
  { name: "Sydney", subdivision: "Australia", lat: -33.8688, lon: 151.2093 },
  { name: "Cairo", subdivision: "Egypt", lat: 30.0444, lon: 31.2357 },
  { name: "Cape Town", subdivision: "South Africa", lat: -33.9249, lon: 18.4241 },
  { name: "Buenos Aires", subdivision: "Argentina", lat: -34.6037, lon: -58.3816 },
  { name: "Nairobi", subdivision: "Kenya", lat: -1.2921, lon: 36.8219 },
  { name: "Dubai", subdivision: "United Arab Emirates", lat: 25.2048, lon: 55.2708 },
  { name: "Singapore", subdivision: "Singapore", lat: 1.3521, lon: 103.8198 },
  { name: "Bangkok", subdivision: "Thailand", lat: 13.7563, lon: 100.5018 },
  { name: "Seoul", subdivision: "South Korea", lat: 37.5665, lon: 126.978 },
  { name: "Berlin", subdivision: "Germany", lat: 52.52, lon: 13.405 },
  { name: "Rome", subdivision: "Italy", lat: 41.9028, lon: 12.4964 },
  { name: "Madrid", subdivision: "Spain", lat: 40.4168, lon: -3.7038 },
  { name: "Istanbul", subdivision: "Turkey", lat: 41.0082, lon: 28.9784 },
  { name: "Riyadh", subdivision: "Saudi Arabia", lat: 24.7136, lon: 46.6753 },
  { name: "Mexico City", subdivision: "Mexico", lat: 19.4326, lon: -99.1332 },
  { name: "Toronto", subdivision: "Canada", lat: 43.6532, lon: -79.3832 },
  { name: "Los Angeles", subdivision: "United States", lat: 34.0522, lon: -118.2437 },
  { name: "Rio de Janeiro", subdivision: "Brazil", lat: -22.9068, lon: -43.1729 },
  { name: "Lagos", subdivision: "Nigeria", lat: 6.5244, lon: 3.3792 },
  { name: "Mumbai", subdivision: "India", lat: 19.076, lon: 72.8777 },
  { name: "Manila", subdivision: "Philippines", lat: 14.5995, lon: 120.9842 }
];

export const countriesData: CountryData[] = [
  {
    name: "Indonesia",
    code: "ID",
    locations: [
      // Aceh
      { name: "Banda Aceh", subdivision: "Aceh", lat: 5.5483, lon: 95.3238 },
      { name: "Sabang", subdivision: "Aceh", lat: 5.8933, lon: 95.3214 },
      { name: "Lhokseumawe", subdivision: "Aceh", lat: 5.1801, lon: 97.1507 },
      { name: "Langsa", subdivision: "Aceh", lat: 4.4694, lon: 97.9719 },
      { name: "Subulussalam", subdivision: "Aceh", lat: 2.6256, lon: 98.0058 },
      // Sumatera Utara
      { name: "Medan", subdivision: "Sumatera Utara", lat: 3.5952, lon: 98.6722 },
      { name: "Binjai", subdivision: "Sumatera Utara", lat: 3.6014, lon: 98.4856 },
      { name: "Tebing Tinggi", subdivision: "Sumatera Utara", lat: 3.3283, lon: 99.1625 },
      { name: "Pematangsiantar", subdivision: "Sumatera Utara", lat: 2.9603, lon: 99.0667 },
      { name: "Tanjungbalai", subdivision: "Sumatera Utara", lat: 2.9667, lon: 99.8 },
      { name: "Sibolga", subdivision: "Sumatera Utara", lat: 1.7397, lon: 98.7903 },
      { name: "Padangsidimpuan", subdivision: "Sumatera Utara", lat: 1.3789, lon: 99.2681 },
      { name: "Gunungsitoli", subdivision: "Sumatera Utara", lat: 1.2894, lon: 97.6167 },
      // Sumatera Barat
      { name: "Padang", subdivision: "Sumatera Barat", lat: -0.9471, lon: 100.4172 },
      { name: "Solok", subdivision: "Sumatera Barat", lat: -0.7967, lon: 100.65 },
      { name: "Sawahlunto", subdivision: "Sumatera Barat", lat: -0.6667, lon: 100.7833 },
      { name: "Padang Panjang", subdivision: "Sumatera Barat", lat: -0.4667, lon: 100.4 },
      { name: "Bukittinggi", subdivision: "Sumatera Barat", lat: -0.3089, lon: 100.3697 },
      { name: "Payakumbuh", subdivision: "Sumatera Barat", lat: -0.2244, lon: 100.6317 },
      { name: "Pariaman", subdivision: "Sumatera Barat", lat: -0.6333, lon: 100.1167 },
      // Riau & Kepulauan Riau
      { name: "Pekanbaru", subdivision: "Riau", lat: 0.5071, lon: 101.4478 },
      { name: "Dumai", subdivision: "Riau", lat: 1.6675, lon: 101.4539 },
      { name: "Batam", subdivision: "Kepulauan Riau", lat: 1.1301, lon: 104.0528 },
      { name: "Tanjungpinang", subdivision: "Kepulauan Riau", lat: 0.9167, lon: 104.45 },
      // Jambi
      { name: "Jambi", subdivision: "Jambi", lat: -1.6101, lon: 103.6131 },
      { name: "Sungai Penuh", subdivision: "Jambi", lat: -2.0628, lon: 101.3917 },
      // Sumatera Selatan & Bangka Belitung
      { name: "Palembang", subdivision: "Sumatera Selatan", lat: -2.9761, lon: 104.7754 },
      { name: "Prabumulih", subdivision: "Sumatera Selatan", lat: -3.4331, lon: 104.225 },
      { name: "Lubuklinggau", subdivision: "Sumatera Selatan", lat: -3.2961, lon: 102.8617 },
      { name: "Pagar Alam", subdivision: "Sumatera Selatan", lat: -4.0153, lon: 103.2656 },
      { name: "Pangkalpinang", subdivision: "Kep. Bangka Belitung", lat: -2.1333, lon: 106.1167 },
      // Bengkulu & Lampung
      { name: "Bengkulu", subdivision: "Bengkulu", lat: -3.7928, lon: 102.2608 },
      { name: "Bandar Lampung", subdivision: "Lampung", lat: -5.3971, lon: 105.2668 },
      { name: "Metro", subdivision: "Lampung", lat: -5.1167, lon: 105.3 },
      // DKI Jakarta
      { name: "Jakarta Pusat", subdivision: "DKI Jakarta", lat: -6.1862, lon: 106.8326 },
      { name: "Jakarta Utara", subdivision: "DKI Jakarta", lat: -6.1214, lon: 106.8746 },
      { name: "Jakarta Barat", subdivision: "DKI Jakarta", lat: -6.1683, lon: 106.7589 },
      { name: "Jakarta Selatan", subdivision: "DKI Jakarta", lat: -6.2615, lon: 106.8106 },
      { name: "Jakarta Timur", subdivision: "DKI Jakarta", lat: -6.225, lon: 106.9004 },
      // Banten
      { name: "Serang", subdivision: "Banten", lat: -6.1167, lon: 106.15 },
      { name: "Cilegon", subdivision: "Banten", lat: -6.0167, lon: 106.0167 },
      { name: "Tangerang", subdivision: "Banten", lat: -6.1781, lon: 106.63 },
      { name: "Tangerang Selatan", subdivision: "Banten", lat: -6.2907, lon: 106.7025 },
      // Jawa Barat
      { name: "Bandung", subdivision: "Jawa Barat", lat: -6.9175, lon: 107.6191 },
      { name: "Bogor", subdivision: "Jawa Barat", lat: -6.5971, lon: 106.806 },
      { name: "Depok", subdivision: "Jawa Barat", lat: -6.4025, lon: 106.7942 },
      { name: "Bekasi", subdivision: "Jawa Barat", lat: -6.2383, lon: 106.9756 },
      { name: "Cimahi", subdivision: "Jawa Barat", lat: -6.8722, lon: 107.5417 },
      { name: "Banjar", subdivision: "Jawa Barat", lat: -7.3719, lon: 108.5419 },
      { name: "Tasikmalaya", subdivision: "Jawa Barat", lat: -7.3274, lon: 108.2207 },
      { name: "Cirebon", subdivision: "Jawa Barat", lat: -6.732, lon: 108.5554 },
      { name: "Sukabumi", subdivision: "Jawa Barat", lat: -6.9278, lon: 106.93 },
      // Jawa Tengah
      { name: "Semarang", subdivision: "Jawa Tengah", lat: -6.9667, lon: 110.4167 },
      { name: "Surakarta", subdivision: "Jawa Tengah", lat: -7.5708, lon: 110.8292 },
      { name: "Salatiga", subdivision: "Jawa Tengah", lat: -7.3303, lon: 110.5084 },
      { name: "Magelang", subdivision: "Jawa Tengah", lat: -7.4797, lon: 110.2183 },
      { name: "Pekalongan", subdivision: "Jawa Tengah", lat: -6.8886, lon: 109.6753 },
      { name: "Tegal", subdivision: "Jawa Tengah", lat: -6.8676, lon: 109.1378 },
      // DI Yogyakarta
      { name: "Yogyakarta", subdivision: "DI Yogyakarta", lat: -7.7956, lon: 110.3695 },
      // Jawa Timur
      { name: "Surabaya", subdivision: "Jawa Timur", lat: -7.2575, lon: 112.7521 },
      { name: "Malang", subdivision: "Jawa Timur", lat: -7.965, lon: 112.63 },
      { name: "Madiun", subdivision: "Jawa Timur", lat: -7.6298, lon: 111.5239 },
      { name: "Kediri", subdivision: "Jawa Timur", lat: -7.8481, lon: 112.0178 },
      { name: "Blitar", subdivision: "Jawa Timur", lat: -8.0983, lon: 112.1681 },
      { name: "Mojokerto", subdivision: "Jawa Timur", lat: -7.4728, lon: 112.4381 },
      { name: "Pasuruan", subdivision: "Jawa Timur", lat: -7.6441, lon: 112.9067 },
      { name: "Probolinggo", subdivision: "Jawa Timur", lat: -7.7569, lon: 113.2161 },
      { name: "Batu", subdivision: "Jawa Timur", lat: -7.8711, lon: 112.5269 },
      // Bali & Nusa Tenggara
      { name: "Denpasar", subdivision: "Bali", lat: -8.6705, lon: 115.2126 },
      { name: "Mataram", subdivision: "Nusa Tenggara Barat", lat: -8.5833, lon: 116.1167 },
      { name: "Bima", subdivision: "Nusa Tenggara Barat", lat: -8.4528, lon: 118.7292 },
      { name: "Kupang", subdivision: "Nusa Tenggara Timur", lat: -10.1667, lon: 123.6 },
      // Kalimantan
      { name: "Pontianak", subdivision: "Kalimantan Barat", lat: -0.0263, lon: 109.3425 },
      { name: "Singkawang", subdivision: "Kalimantan Barat", lat: 0.9022, lon: 108.9867 },
      { name: "Palangkaraya", subdivision: "Kalimantan Tengah", lat: -2.2083, lon: 113.9167 },
      { name: "Banjarmasin", subdivision: "Kalimantan Selatan", lat: -3.3167, lon: 114.59 },
      { name: "Banjarbaru", subdivision: "Kalimantan Selatan", lat: -3.4431, lon: 114.8306 },
      { name: "Samarinda", subdivision: "Kalimantan Timur", lat: -0.5022, lon: 117.1536 },
      { name: "Balikpapan", subdivision: "Kalimantan Timur", lat: -1.2653, lon: 116.8312 },
      { name: "Bontang", subdivision: "Kalimantan Timur", lat: 0.125, lon: 117.475 },
      { name: "Tarakan", subdivision: "Kalimantan Utara", lat: 3.3283, lon: 117.59 },
      { name: "Tanjung Selor", subdivision: "Kalimantan Utara", lat: 2.8364, lon: 117.3653 },
      // Sulawesi
      { name: "Manado", subdivision: "Sulawesi Utara", lat: 1.4889, lon: 124.8428 },
      { name: "Bitung", subdivision: "Sulawesi Utara", lat: 1.4404, lon: 125.1217 },
      { name: "Tomohon", subdivision: "Sulawesi Utara", lat: 1.3256, lon: 124.8392 },
      { name: "Kotamobagu", subdivision: "Sulawesi Utara", lat: 0.7303, lon: 124.3128 },
      { name: "Gorontalo", subdivision: "Gorontalo", lat: 0.5435, lon: 123.0568 },
      { name: "Palu", subdivision: "Sulawesi Tengah", lat: -0.8917, lon: 119.8708 },
      { name: "Mamuju", subdivision: "Sulawesi Barat", lat: -2.6772, lon: 118.8895 },
      { name: "Makassar", subdivision: "Sulawesi Selatan", lat: -5.1477, lon: 119.4328 },
      { name: "Parepare", subdivision: "Sulawesi Selatan", lat: -4.015, lon: 119.63 },
      { name: "Palopo", subdivision: "Sulawesi Selatan", lat: -2.9983, lon: 120.19 },
      { name: "Kendari", subdivision: "Sulawesi Tenggara", lat: -3.9985, lon: 122.5129 },
      { name: "Baubau", subdivision: "Sulawesi Tenggara", lat: -5.4678, lon: 122.6042 },
      // Maluku
      { name: "Ambon", subdivision: "Maluku", lat: -3.6954, lon: 128.1814 },
      { name: "Tual", subdivision: "Maluku", lat: -5.6301, lon: 132.7383 },
      // Maluku Utara
      { name: "Sofifi", subdivision: "Maluku Utara", lat: 0.74, lon: 127.59 },
      { name: "Ternate", subdivision: "Maluku Utara", lat: 0.7901, lon: 127.3828 },
      { name: "Tidore", subdivision: "Maluku Utara", lat: 0.6908, lon: 127.4239 },
      // Papua
      { name: "Jayapura", subdivision: "Papua", lat: -2.5337, lon: 140.7181 },
      { name: "Manokwari", subdivision: "Papua Barat", lat: -0.8667, lon: 134.0833 },
      { name: "Sorong", subdivision: "Papua Barat Daya", lat: -0.88, lon: 131.25 },
      { name: "Merauke", subdivision: "Papua Selatan", lat: -8.4991, lon: 140.4047 },
      { name: "Nabire", subdivision: "Papua Tengah", lat: -3.3667, lon: 135.5 },
      { name: "Wamena", subdivision: "Papua Pegunungan", lat: -4.0972, lon: 138.9464 }
    ]
  },
  {
    name: "United States",
    code: "US",
    locations: [
      { name: "Washington D.C.", subdivision: "District of Columbia", lat: 38.9072, lon: -77.0369 },
      { name: "Sacramento", subdivision: "California", lat: 38.5816, lon: -121.4944 },
      { name: "Austin", subdivision: "Texas", lat: 30.2672, lon: -97.7431 },
      { name: "Albany", subdivision: "New York", lat: 42.6526, lon: -73.7562 },
      { name: "Tallahassee", subdivision: "Florida", lat: 30.4383, lon: -84.2807 },
      { name: "Springfield", subdivision: "Illinois", lat: 39.7817, lon: -89.6501 },
      { name: "Harrisburg", subdivision: "Pennsylvania", lat: 40.2732, lon: -76.8867 },
      { name: "Columbus", subdivision: "Ohio", lat: 39.9612, lon: -82.9988 },
      { name: "Atlanta", subdivision: "Georgia", lat: 33.749, lon: -84.388 },
      { name: "Olympia", subdivision: "Washington", lat: 47.0379, lon: -122.9007 },
      { name: "Boston", subdivision: "Massachusetts", lat: 42.3601, lon: -71.0589 },
      { name: "Denver", subdivision: "Colorado", lat: 39.7392, lon: -104.9903 },
      { name: "Phoenix", subdivision: "Arizona", lat: 33.4484, lon: -112.074 },
      { name: "Lansing", subdivision: "Michigan", lat: 42.7325, lon: -84.5555 },
      { name: "Raleigh", subdivision: "North Carolina", lat: 35.7796, lon: -78.6382 }
    ]
  },
  {
    name: "China",
    code: "CN",
    locations: [
      { name: "Beijing", subdivision: "Beijing Municipality", lat: 39.9042, lon: 116.4074 },
      { name: "Shanghai", subdivision: "Shanghai Municipality", lat: 31.2304, lon: 121.4737 },
      { name: "Guangzhou", subdivision: "Guangdong", lat: 23.1291, lon: 113.2644 },
      { name: "Chengdu", subdivision: "Sichuan", lat: 30.5728, lon: 104.0668 },
      { name: "Hangzhou", subdivision: "Zhejiang", lat: 30.2741, lon: 120.1551 },
      { name: "Wuhan", subdivision: "Hubei", lat: 30.5928, lon: 114.3055 },
      { name: "Xi'an", subdivision: "Shaanxi", lat: 34.3416, lon: 108.9398 },
      { name: "Chongqing", subdivision: "Chongqing Municipality", lat: 29.563, lon: 106.5516 },
      { name: "Nanjing", subdivision: "Jiangsu", lat: 32.0603, lon: 118.7969 },
      { name: "Jinan", subdivision: "Shandong", lat: 36.6512, lon: 117.12 },
      { name: "Kunming", subdivision: "Yunnan", lat: 25.0422, lon: 102.7122 },
      { name: "Harbin", subdivision: "Heilongjiang", lat: 45.8038, lon: 126.5358 },
      { name: "Urumqi", subdivision: "Xinjiang", lat: 43.8256, lon: 87.6168 },
      { name: "Lhasa", subdivision: "Tibet", lat: 29.6524, lon: 91.1172 },
      { name: "Fuzhou", subdivision: "Fujian", lat: 26.0745, lon: 119.2965 }
    ]
  },
  {
    name: "India",
    code: "IN",
    locations: [
      { name: "New Delhi", subdivision: "National Capital Territory", lat: 28.6139, lon: 77.209 },
      { name: "Mumbai", subdivision: "Maharashtra", lat: 19.076, lon: 72.8777 },
      { name: "Kolkata", subdivision: "West Bengal", lat: 22.5726, lon: 88.3639 },
      { name: "Chennai", subdivision: "Tamil Nadu", lat: 13.0827, lon: 80.2707 },
      { name: "Bengaluru", subdivision: "Karnataka", lat: 12.9716, lon: 77.5946 },
      { name: "Hyderabad", subdivision: "Telangana", lat: 17.385, lon: 78.4867 },
      { name: "Gandhinagar", subdivision: "Gujarat", lat: 23.2156, lon: 72.6369 },
      { name: "Lucknow", subdivision: "Uttar Pradesh", lat: 26.8467, lon: 80.9462 },
      { name: "Patna", subdivision: "Bihar", lat: 25.5941, lon: 85.1376 },
      { name: "Jaipur", subdivision: "Rajasthan", lat: 26.9124, lon: 75.7873 },
      { name: "Thiruvananthapuram", subdivision: "Kerala", lat: 8.5241, lon: 76.9366 },
      { name: "Bhopal", subdivision: "Madhya Pradesh", lat: 23.2599, lon: 77.4126 },
      { name: "Bhubaneswar", subdivision: "Odisha", lat: 20.2961, lon: 85.8245 },
      { name: "Guwahati", subdivision: "Assam", lat: 26.1445, lon: 91.7362 },
      { name: "Srinagar", subdivision: "Jammu and Kashmir", lat: 34.0837, lon: 74.7973 }
    ]
  },
  {
    name: "Brazil",
    code: "BR",
    locations: [
      { name: "Brasília", subdivision: "Distrito Federal", lat: -15.7938, lon: -47.8828 },
      { name: "Rio de Janeiro", subdivision: "Rio de Janeiro", lat: -22.9068, lon: -43.1729 },
      { name: "São Paulo", subdivision: "São Paulo", lat: -23.5505, lon: -46.6333 },
      { name: "Belo Horizonte", subdivision: "Minas Gerais", lat: -19.9173, lon: -43.9345 },
      { name: "Salvador", subdivision: "Bahia", lat: -12.9714, lon: -38.5014 },
      { name: "Fortaleza", subdivision: "Ceará", lat: -3.7319, lon: -38.5267 },
      { name: "Manaus", subdivision: "Amazonas", lat: -3.119, lon: -60.0217 },
      { name: "Recife", subdivision: "Pernambuco", lat: -8.0543, lon: -34.8813 },
      { name: "Porto Alegre", subdivision: "Rio Grande do Sul", lat: -30.0346, lon: -51.2177 },
      { name: "Belém", subdivision: "Pará", lat: -1.4558, lon: -48.4902 },
      { name: "Curitiba", subdivision: "Paraná", lat: -25.4284, lon: -49.2733 },
      { name: "Cuiabá", subdivision: "Mato Grosso", lat: -15.601, lon: -56.0974 }
    ]
  },
  {
    name: "Russia",
    code: "RU",
    locations: [
      { name: "Moscow", subdivision: "Moscow Federal City", lat: 55.7558, lon: 37.6173 },
      { name: "Saint Petersburg", subdivision: "Saint Petersburg Federal City", lat: 59.9343, lon: 30.3351 },
      { name: "Novosibirsk", subdivision: "Novosibirsk Oblast", lat: 55.0084, lon: 82.9357 },
      { name: "Yekaterinburg", subdivision: "Sverdlovsk Oblast", lat: 56.8389, lon: 60.6057 },
      { name: "Nizhny Novgorod", subdivision: "Nizhny Novgorod Oblast", lat: 56.3269, lon: 44.0059 },
      { name: "Kazan", subdivision: "Republic of Tatarstan", lat: 55.7887, lon: 49.1221 },
      { name: "Vladivostok", subdivision: "Primorsky Krai", lat: 43.1198, lon: 131.8869 },
      { name: "Sochi", subdivision: "Krasnodar Krai", lat: 43.6028, lon: 39.7342 },
      { name: "Krasnoyarsk", subdivision: "Krasnoyarsk Krai", lat: 56.0153, lon: 92.8932 },
      { name: "Irkutsk", subdivision: "Irkutsk Oblast", lat: 52.287, lon: 104.305 },
      { name: "Murmansk", subdivision: "Murmansk Oblast", lat: 68.9585, lon: 33.0827 },
      { name: "Khabarovsk", subdivision: "Khabarovsk Krai", lat: 48.4725, lon: 135.0577 }
    ]
  },
  {
    name: "Germany",
    code: "DE",
    locations: [
      { name: "Berlin", subdivision: "Berlin State", lat: 52.52, lon: 13.405 },
      { name: "Hamburg", subdivision: "Hamburg State", lat: 53.5511, lon: 9.9937 },
      { name: "Munich", subdivision: "Bavaria", lat: 48.1351, lon: 11.582 },
      { name: "Düsseldorf", subdivision: "North Rhine-Westphalia", lat: 51.2277, lon: 6.7735 },
      { name: "Stuttgart", subdivision: "Baden-Württemberg", lat: 48.7758, lon: 9.1829 },
      { name: "Frankfurt", subdivision: "Hesse", lat: 50.1109, lon: 8.6821 },
      { name: "Hanover", subdivision: "Lower Saxony", lat: 52.3759, lon: 9.732 },
      { name: "Bremen", subdivision: "Bremen State", lat: 53.0793, lon: 8.8017 },
      { name: "Dresden", subdivision: "Saxony", lat: 51.0504, lon: 13.7373 },
      { name: "Kiel", subdivision: "Schleswig-Holstein", lat: 54.3233, lon: 10.1228 },
      { name: "Potsdam", subdivision: "Brandenburg", lat: 52.3989, lon: 13.0657 },
      { name: "Schwerin", subdivision: "Mecklenburg-Vorpommern", lat: 53.6355, lon: 11.4012 }
    ]
  },
  {
    name: "Japan",
    code: "JP",
    locations: [
      { name: "Tokyo", subdivision: "Tokyo Prefecture", lat: 35.6762, lon: 139.6503 },
      { name: "Kyoto", subdivision: "Kyoto Prefecture", lat: 35.0116, lon: 135.7681 },
      { name: "Osaka", subdivision: "Osaka Prefecture", lat: 34.6937, lon: 135.5023 },
      { name: "Nagoya", subdivision: "Aichi", lat: 35.1815, lon: 136.9064 },
      { name: "Yokohama", subdivision: "Kanagawa", lat: 35.4437, lon: 139.638 },
      { name: "Sapporo", subdivision: "Hokkaido", lat: 43.0618, lon: 141.3545 },
      { name: "Fukuoka", subdivision: "Fukuoka Prefecture", lat: 33.5904, lon: 130.4017 },
      { name: "Hiroshima", subdivision: "Hiroshima Prefecture", lat: 34.3853, lon: 132.4553 },
      { name: "Sendai", subdivision: "Miyagi", lat: 38.2682, lon: 140.8694 },
      { name: "Naha", subdivision: "Okinawa", lat: 26.2124, lon: 127.6809 },
      { name: "Kobe", subdivision: "Hyogo", lat: 34.6901, lon: 135.1955 },
      { name: "Kanazawa", subdivision: "Ishikawa", lat: 36.5613, lon: 136.6562 }
    ]
  },
  {
    name: "United Kingdom",
    code: "GB",
    locations: [
      { name: "London", subdivision: "England", lat: 51.5074, lon: -0.1278 },
      { name: "Edinburgh", subdivision: "Scotland", lat: 55.9533, lon: -3.1883 },
      { name: "Belfast", subdivision: "Northern Ireland", lat: 54.5973, lon: -5.9301 },
      { name: "Cardiff", subdivision: "Wales", lat: 51.4816, lon: -3.1791 },
      { name: "Birmingham", subdivision: "West Midlands", lat: 52.4862, lon: -1.8904 },
      { name: "Manchester", subdivision: "Greater Manchester", lat: 53.4808, lon: -2.2426 },
      { name: "Glasgow", subdivision: "Scotland", lat: 55.8642, lon: -4.2518 },
      { name: "Liverpool", subdivision: "Merseyside", lat: 53.4084, lon: -2.9916 },
      { name: "Leeds", subdivision: "West Yorkshire", lat: 53.8008, lon: -1.5491 },
      { name: "Bristol", subdivision: "Bristol County", lat: 51.4545, lon: -2.5879 }
    ]
  },
  {
    name: "Australia",
    code: "AU",
    locations: [
      { name: "Canberra", subdivision: "Australian Capital Territory", lat: -35.2809, lon: 149.13 },
      { name: "Sydney", subdivision: "New South Wales", lat: -33.8688, lon: 151.2093 },
      { name: "Melbourne", subdivision: "Victoria", lat: -37.8136, lon: 144.9631 },
      { name: "Brisbane", subdivision: "Queensland", lat: -27.4698, lon: 153.0251 },
      { name: "Perth", subdivision: "Western Australia", lat: -31.9505, lon: 115.8605 },
      { name: "Adelaide", subdivision: "South Australia", lat: -34.9285, lon: 138.6007 },
      { name: "Hobart", subdivision: "Tasmania", lat: -42.8821, lon: 147.3272 },
      { name: "Darwin", subdivision: "Northern Territory", lat: -12.4634, lon: 130.8456 }
    ]
  }
];
