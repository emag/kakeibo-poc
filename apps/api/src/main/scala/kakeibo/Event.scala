package kakeibo

import java.time.Instant

trait Event {
  val id: String
  val timestamp: Instant
  val eventType: String
}
