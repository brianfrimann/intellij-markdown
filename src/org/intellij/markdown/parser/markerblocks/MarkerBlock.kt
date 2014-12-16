package org.intellij.markdown.parser.markerblocks

import org.intellij.markdown.IElementType
import org.intellij.markdown.parser.MarkdownConstraints
import org.intellij.markdown.parser.ProductionHolder
import org.intellij.markdown.parser.TokensCache

public trait MarkerBlock {

    public fun processToken(tokenType: IElementType, builder: TokensCache.Iterator, currentConstraints: MarkdownConstraints): ProcessingResult

    public fun getBlockConstraints(): MarkdownConstraints

    /**
     * @param action to accept
     * @return true if this block is to be deleted after this action, false otherwise
     */
    public fun acceptAction(action: ClosingAction): Boolean

    public enum class ClosingAction {
        DONE {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
                marker.done(type)
            }
        }
        DROP {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
            }
        }
        DEFAULT {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
                throw UnsupportedOperationException("Should not be invoked")
            }
        }
        NOTHING {
            override fun doAction(marker: ProductionHolder.Marker, type: IElementType) {
            }
        }

        public abstract fun doAction(marker: ProductionHolder.Marker, `type`: IElementType)
    }

    public enum class EventAction {
        PROPAGATE
        CANCEL
    }

    class object {


        public fun ProcessingResult(childrenAction: ClosingAction, selfAction: ClosingAction, eventAction: EventAction): ProcessingResult {
            return ProcessingResult(childrenAction, selfAction, eventAction, false)
        }

        public class ProcessingResult internal (public val childrenAction: ClosingAction,
                                                public val selfAction: ClosingAction,
                                                public val eventAction: EventAction,
                                                public val isPostponed: Boolean) {

            public fun postpone(): ProcessingResult {
                if (isPostponed) {
                    return this
                }

                return ProcessingResult(childrenAction, selfAction, eventAction, true)
            }

            class object {
                public val PASS: ProcessingResult = ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.PROPAGATE)
                public val CANCEL: ProcessingResult = ProcessingResult(ClosingAction.NOTHING, ClosingAction.NOTHING, EventAction.CANCEL)
                public val DEFAULT: ProcessingResult = ProcessingResult(ClosingAction.DEFAULT, ClosingAction.DONE, EventAction.PROPAGATE)
            }

        }
    }

}