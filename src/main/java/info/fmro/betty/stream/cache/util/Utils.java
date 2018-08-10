package info.fmro.betty.stream.cache.util;

/**
 * Utils class
 */
public class Utils {
    public static double selectPrice(boolean isImage, double currentPrice, Double newPrice) {
        if (isImage) {
            return newPrice == null ? 0.0 : newPrice;
        } else {
            return newPrice == null ? currentPrice : newPrice;
        }
    }

//    public static void printMarket(MarketSnap market) {
//        market.getMarketRunners().sort((mr1, mr2) -> Integer.compare(mr1.getDefinition().getSortPriority(), mr2.getDefinition().getSortPriority()));
//
//        final List<MarketDetailsRow> marketDetails = new ArrayList<>();
//
//        for (MarketRunnerSnap runner : market.getMarketRunners()) {
//            final MarketRunnerPrices snap = runner.getPrices();
//            final MarketDetailsRow marketDetail = new MarketDetailsRow(market.getMarketId(),
//                                                                       runner.getRunnerId().getSelectionId(),
//                                                                       getLevel(snap.getBdatb(), 0).getPrice(),
//                                                                       getLevel(snap.getBdatb(), 0).getSize(),
//                                                                       getLevel(snap.getBdatl(), 0).getPrice(),
//                                                                       getLevel(snap.getBdatl(), 0).getSize());
//            marketDetails.add(marketDetail);
//        }
//
//        final BeanListTableModel<MarketDetailsRow> model = new BeanListTableModel<>(marketDetails, createHeader("marketId", "selectionId", "batbPrice", "batbSize", "batlPrice", "batlSize"));
//        renderTable(model);
//    }

//    public static void printOrderMarket(OrderMarketSnap orderMarketSnap) {
//        System.out.println("Orders  (marketId=" + orderMarketSnap.getMarketId() + ")");
//
//        final List<Order> orders = new ArrayList<>();
//
//        orderMarketSnap.getOrderMarketRunners().forEach(orderMarketRunnerSnap -> {
//            orders.addAll(orderMarketRunnerSnap.getUnmatchedOrders().values());
//        });
//
//        final BeanListTableModel<Order> model = new BeanListTableModel<>(orders, createHeader("id", "side", "pt", "ot", "status", "sv", "p", "sc", "rc", "s", "pd", "rac", "md", "sl", "avp", "sm", "bsp", "sr"));
//        renderTable(model);
//    }

//    public static void renderTable(BeanListTableModel model) {
//        org.springframework.shell.table.TableBuilder tableBuilder = new org.springframework.shell.table.TableBuilder(model);
//        tableBuilder.addHeaderAndVerticalsBorders(BorderStyle.oldschool);
//        Table table = tableBuilder.build();
//        final String rendered = table.render(1000);
//        System.out.println(rendered);
//    }
//
//    public static LinkedHashMap<String, Object> createHeader(String... headers) {
//        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
//        for (String header : headers) {
//            result.put(header, header);
//        }
//        return result;
//    }
//
//    public static LevelPriceSize getLevel(List<LevelPriceSize> values, int level) {
//        return !values.isEmpty() ? values.get(0) : new LevelPriceSize(level, 0, 0);
//    }
}
