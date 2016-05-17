/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Wasi
 */
public class UserSession {

    private final ArrayList<UserQuery> allQueriesInSession;
    private final HashMap<Integer, HashSet<String>> originalQueryToCoverQuery;

    public UserSession() {
        allQueriesInSession = new ArrayList<>();
        originalQueryToCoverQuery = new HashMap<>();
    }

    /**
     *
     * @param query
     * @param queryId
     * @param queryTopicNo
     */
    public void addUserQuery(String query, int queryId, int queryTopicNo) {
        UserQuery userQuery = new UserQuery(queryId, query, queryTopicNo);
        allQueriesInSession.add(userQuery);
    }

    /**
     *
     * @param origQueryId
     * @param coverQueries
     */
    public void addCoverQuery(int origQueryId, ArrayList<String> coverQueries) {
        HashSet<String> tempSet = new HashSet<>(coverQueries);
        originalQueryToCoverQuery.put(origQueryId, tempSet);
    }

    /**
     * Checks whether a query is repeated in the same user session.
     *
     * @param query
     * @return
     */
    public int isRepeatedQuery(String query) {
        for (UserQuery userQuery : allQueriesInSession) {
            if (userQuery.getQuery().equals(query)) {
                return userQuery.getQueryId();
            }
        }
        return -1;
    }

    /**
     *
     * @return
     */
    public int getLastQueryTopicNo() {
        if (allQueriesInSession.isEmpty()) {
            return -1;
        }
        return allQueriesInSession.get(allQueriesInSession.size() - 1).getQueryTopicNo();
    }

    /**
     * Return all cover queries which were generated previously for a user
     * query.
     *
     * @param queryId
     * @return
     */
    public ArrayList<String> getCoverQueries(int queryId) {
        ArrayList<String> coverQueries = new ArrayList<>();
        for (String coverQuery : originalQueryToCoverQuery.get(queryId)) {
            coverQueries.add(coverQuery);
        }
        return coverQueries;
    }
}

class UserQuery {

    private final int queryId;
    private final String query;
    private final int queryTopicNo;

    public UserQuery(int queryId, String query, int queryTopicNo) {
        this.queryId = queryId;
        this.query = query;
        this.queryTopicNo = queryTopicNo;
    }

    public int getQueryId() {
        return queryId;
    }

    public String getQuery() {
        return query;
    }

    public int getQueryTopicNo() {
        return queryTopicNo;
    }
}
