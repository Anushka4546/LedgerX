package com.ledgerx.engine.decision;

import com.ledgerx.core.action.Action;

import java.util.Map;

public class ActionRegistry {

    private final Map<String, Action> actions;

    public ActionRegistry(Map<String, Action> actions) {
        this.actions = actions;
    }

    public Action get(String id) {
        return actions.get(id);
    }
}
