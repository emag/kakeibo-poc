package kakeibo

import java.time.Instant

trait Event[A] {
  val id: String
  val payload: A
  val timestamp: Instant
  val eventType: String
}
