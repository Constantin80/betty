package info.fmro.betty.stream.enums;

public enum FilterFlag {
    //    Filter name                Fields:           Type                  Description
//    EX_BEST_OFFERS_DISP       bdatb, bdatl        level, price, size      Best prices including virtual prices - depth is controlled by ladderLevels (1 to 10)
//    EX_BEST_OFFERS            batb, batl          level, price, size      Best prices not including virtual prices - depth is controlled by ladderLevels (1 to 10)
//    EX_ALL_OFFERS             atb, atl            price, size             Full available to BACK/LAY ladder
//    EX_TRADED                 trd                 price, size             Full traded ladder
//    EX_TRADED_VOL             tv                  size                    Market and runner level traded volume
//    EX_LTP                    ltp                 price                   Last traded price
//    EX_MARKET_DEF             marketDefinition    MarketDefinition        Send market definitions.
//    SP_TRADED                 spb, spl            price, size             Starting price ladder
//    SP_PROJECTED              spn, spf            price                   Starting price projection prices
    EX_BEST_OFFERS_DISP,
    EX_BEST_OFFERS,
    EX_ALL_OFFERS,
    EX_TRADED,
    EX_TRADED_VOL,
    EX_LTP,
    EX_MARKET_DEF,
    SP_TRADED,
    SP_PROJECTED
}
