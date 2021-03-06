package org.stt.command

import org.antlr.v4.runtime.tree.RuleNode
import org.stt.grammar.EnglishCommandsBaseVisitor
import org.stt.grammar.EnglishCommandsParser
import org.stt.grammar.EnglishCommandsParser.CommandContext
import org.stt.model.TimeTrackingItem
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalQueries

class CommandTextParser(private val formatters: List<DateTimeFormatter>) {
    private val parserVisitor = MyEnglishCommandsBaseVisitor()

    internal fun walk(commandContext: CommandContext): Any {
        return commandContext.accept(parserVisitor)
    }

    private inner class MyEnglishCommandsBaseVisitor : EnglishCommandsBaseVisitor<Any>() {
        override fun visitDate(ctx: EnglishCommandsParser.DateContext): LocalDate {
            ctx.text
            return LocalDate.parse(ctx.text)
        }

        override fun visitDateTime(ctx: EnglishCommandsParser.DateTimeContext): LocalDateTime {
            val temporalAccessor = formatters
                    .mapNotNull { formatter ->
                        try {
                            return@mapNotNull formatter.parse(ctx.text)
                        } catch (e: DateTimeParseException) {
                            return@mapNotNull null
                        }
                    }
                    .firstOrNull() ?: throw DateTimeParseException("Invalid date format", ctx.text, 0)
            val date = temporalAccessor.query(TemporalQueries.localDate())
            val time = temporalAccessor.query(TemporalQueries.localTime())
            return LocalDateTime.of(date ?: LocalDate.now(), time)
        }

        override fun visitSinceFormat(ctx: EnglishCommandsParser.SinceFormatContext): Array<LocalDateTime?> {
            return arrayOf(visitDateTime(ctx.start), if (ctx.end != null) visitDateTime(ctx.end) else null)
        }

        override fun visitResumeLastCommand(ctx: EnglishCommandsParser.ResumeLastCommandContext): Any {
            return ResumeLastActivity(LocalDateTime.now())
        }

        override fun visitAgoFormat(ctx: EnglishCommandsParser.AgoFormatContext): Array<LocalDateTime?> {

            val amount = ctx.amount
            val timeUnit = ctx.timeUnit()
            val duration = when {
                timeUnit.HOURS() != null -> Duration.ofHours(amount.toLong())
                timeUnit.MINUTES() != null -> Duration.ofMinutes(amount.toLong())
                timeUnit.SECONDS() != null -> Duration.ofSeconds(amount.toLong())
                else -> throw IllegalStateException("Unknown ago unit: " + ctx.text)
            }
            return arrayOf(LocalDateTime.now().minus(duration), null)
        }

        override fun visitFromToFormat(ctx: EnglishCommandsParser.FromToFormatContext): Array<LocalDateTime?> {
            val start = visitDateTime(ctx.start)
            val end = if (ctx.end != null) visitDateTime(ctx.end) else null
            return arrayOf(start, end)
        }

        override fun visitTimeFormat(ctx: EnglishCommandsParser.TimeFormatContext): Array<LocalDateTime?> {
            return super.visitTimeFormat(ctx) as? Array<LocalDateTime?>
                    ?: return arrayOf(LocalDateTime.now(), null)
        }

        override fun visitItemWithComment(ctx: EnglishCommandsParser.ItemWithCommentContext): Any {
            val period = visitTimeFormat(ctx.timeFormat())
            return if (period[1] != null) {
                TimeTrackingItem(ctx.text, period[0]!!, period[1])
            } else {
                TimeTrackingItem(ctx.text, period[0]!!)
            }
        }

        override fun visitFinCommand(ctx: EnglishCommandsParser.FinCommandContext): LocalDateTime {
            return if (ctx.at != null) {
                visitDateTime(ctx.at)
            } else LocalDateTime.now()
        }

        override fun shouldVisitNextChild(node: RuleNode?, currentResult: Any?): Boolean {
            return currentResult == null
        }
    }
}
