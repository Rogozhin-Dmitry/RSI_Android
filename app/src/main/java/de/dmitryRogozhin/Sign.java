package de.dmitryRogozhin;


import android.util.Log;

class Sign {
    private String name;
    private String text;
    private int image;

    public Sign(int id) {
        this.setName(id);
        this.setText(id);
        this.setImage(id);
    }

    public void setName(int id) {
        this.name = switch (id) {
            case (0) -> "Главная дорога";
            case (1) -> "Уступите дорогу";
            case (2) -> "Стоп-линия";
            case (3) -> "Остановка запрещена";
            case (4) -> "Въезд запрещен";
            case (5) -> "Железнодорожный переезд без шлагбаума";
            default -> null;
        };
    }

    public void setText(int id) {
        this.text = switch (id) {
            case (4) -> "Запрещается въезд всех транспортных средств в данном направлении. Наказ" +
                    "ание - КоАП РФ 12.16 ч. 3, КоАП РФ 12.16 ч. 3.1 (повторное нарушение)";
            case (0) -> "Дорога, на которой предоставлено право преимущественного проезда нерегу" +
                    "лируемых перекрестков. Устанавливается непосредственно перед перекрестком. " +
                    "Наказание - КоАП РФ 12.13 ч. 2";
            case (5) -> "Предупреждает о приближении к ж/д переезду без шлагбаума. Вне населённо"
                    + "го пункта устанавливается на расстоянии 150-300 м, в населённом пункте — " +
                    "на расстоянии 50-100 м. Наказание - КоАП РФ 12.10 ч. 1, КоАП РФ 12.10 ч. 2 " +
                    "(нарушение правил проезда, за исключением случаев, предусмотренных частью 1" +
                    "), КоАП РФ 12.10 ч. 3 (повторное нарушение)";
            case (3) -> "Запрещаются остановка и стоянка транспортных средств. Распространяется "
                    + "только на ту сторону дороги, на которой они установлены. Наказание - КоАП" +
                    " РФ 12.19 ч. 1 (Россия), КоАП РФ 12.19 ч. 5 (Москва, Санкт-Петербург)";
            case (2) -> "Место остановки транспортных средств при запрещающем сигнале светофора "
                    + "(регулировщика). Наказание - КоАП 12.12 ч. 2";
            case (1) -> "Водитель должен уступить дорогу транспортным средствам, движущимся по п"
                    + "ересекаемой дороге. Наказание - КоАП РФ 12.13 ч. 2";
            default -> null;
        };
    }

    public void setImage(int id) {
        try {
            int fileName = switch (id) {
                case (0) -> R.drawable.sign0;
                case (1) -> R.drawable.sign1;
                case (2) -> R.drawable.sign2;
                case (3) -> R.drawable.sign3;
                case (4) -> R.drawable.sign4;
                case (5) -> R.drawable.sign5;
                default -> -10000000;
            };
            if (fileName == -10000000) {
                return;
            }
            this.image = fileName;
        } catch (Exception e) {
            Log.e("SignInfo", "Error: " + e.getMessage());
        }
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.text;
    }

    public int getImage() {
        return this.image;
    }
}