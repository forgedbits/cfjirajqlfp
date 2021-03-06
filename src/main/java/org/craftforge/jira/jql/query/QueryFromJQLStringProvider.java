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
package org.craftforge.jira.jql.query;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.parser.DefaultJqlQueryParser;
import com.atlassian.jira.jql.parser.JqlQueryParser;
import com.atlassian.query.Query;

/**
 *
 * @author pbojko
 */
public class QueryFromJQLStringProvider implements QueryProvider {
	
	private JqlQueryParser parser;

	public QueryFromJQLStringProvider() {
		this.parser = new DefaultJqlQueryParser();
	}

	public Query provide(User user, String buildingStr) throws Exception {
		return parser.parseQuery(buildingStr);
	}
	
}
