/*
 * Copyright 2012 Craftware Sp. z o.o..
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
 */
package org.craftforge.jira.jql.linkedissues.collectors;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.LinkCollection;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author pbojko
 */
public class OutwardLinkedIssueCollector implements LinkedIssueCollector {

	private static final String OUTWARD_DRIRECTION = "Outward";

	public Collection<Issue> collectIfNeeded(String relation, String direction, LinkCollection linkCollection) {
		if (relation != null && !relation.isEmpty() && (direction == null || direction.equalsIgnoreCase(OUTWARD_DRIRECTION))) {
			return linkCollection.getOutwardIssues(relation);
		} else {
			return Collections.<Issue>emptyList();
		}
	}
}
