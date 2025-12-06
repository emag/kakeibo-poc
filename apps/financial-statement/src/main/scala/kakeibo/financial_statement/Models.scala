package kakeibo.financial_statement

case class BS(
    aggId: Int,
    asset: BigDecimal,
    liability: BigDecimal
)

case class PL(
    aggId: Int,
    expense: BigDecimal,
    revenue: BigDecimal
)
