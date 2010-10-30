/*
 * Copyright 2010 Craftware Sp. z o.o..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package org.craftforge.jira.jql;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.opensymphony.user.User;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pbojko
 */
public class SubtaskIssuesFromFilterFunction extends AbstractIssuesFromFilterFunction {

	@Override
	public String getFunctionName() {
		return "subtaskIssuesFromFilter";
	}

	public List<QueryLiteral> getValues(QueryCreationContext qcc, FunctionOperand fo, TerminalClause tc) {
		List<Issue> issues = fetchIssuesFromSubfilter(qcc, fo);
		issues = fetchSubtasks(qcc.getUser(), issues);
		return convertToQueryLiteraCollection(fo, issues);
	}

	public int getMinimumNumberOfExpectedArguments() {
		return 1;
	}

	private List<Issue> fetchSubtasks(User user, List<Issue> issues) {
		List<Issue> result = new ArrayList<Issue>();
		for (Issue issue : issues) {
			result.addAll(issue.getSubTaskObjects());
		}
		return result;
	}
}
