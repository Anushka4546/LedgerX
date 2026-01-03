package com.ledgerx.core.decision;

import com.ledgerx.core.domain.RefundAction;
import com.ledgerx.core.domain.RefundState;

import java.util.List;

public final class Decision {

    public final RefundState state;
    public final RefundAction action;
    public final double confidence;
    public final double expectedLossRetry;
    public final double expectedLossWait;
    public final List<String> reasons;

       public Decision(
            RefundState state,
            RefundAction action,
            double confidence,
            double expectedLossRetry,
            double expectedLossWait,
            List<String> reasons) {

        this.state = state;
        this.action = action;
        this.confidence = confidence;
        this.expectedLossRetry = expectedLossRetry;
        this.expectedLossWait = expectedLossWait;
        this.reasons = reasons;
    }


    public static Decision retryNow(
        double confidence,
        double retryLoss,
        double waitLoss,
        List<String> reasons) {

        return new Decision(
                RefundState.SAFE_TO_ISSUE,
                RefundAction.RETRY_NOW,
                confidence,
                retryLoss,
                waitLoss,
                reasons
        );
    }

    public static Decision wait(
            double confidence,
            double retryLoss,
            double waitLoss,
            List<String> reasons) {

        return new Decision(
                RefundState.WAITING_FOR_SETTLEMENT,
                RefundAction.WAIT,
                confidence,
                retryLoss,
                waitLoss,
                reasons
        );
    }

    public static Decision escalated(
            double confidence,
            List<String> reasons) {

        return new Decision(
                RefundState.ESCALATED,
                RefundAction.ESCALATE,
                confidence,
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                reasons
        );
    }

    public static Decision blocked(List<String> reasons) {
        return new Decision(
                RefundState.BLOCKED,
                RefundAction.ESCALATE,
                0.0,
                Double.POSITIVE_INFINITY,
                0.0,
                reasons
        );
    }

}
