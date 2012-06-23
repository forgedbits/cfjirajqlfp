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
package org.craftforge.jira.jql;

import com.atlassian.jira.JiraException;
import com.atlassian.query.Query;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pbojko
 */
public class QueryLocalGate {

	private final static ThreadLocal<HolderMap> TL = new ThreadLocal<HolderMap>() {
		@Override
		protected HolderMap initialValue() {
			return new HolderMap();
		}
	};

	public static void enter(final Query query) throws JiraException {
		TL.get().enterForId(query.getQueryString());
	}

	public static void leave(final Query query) throws JiraException {
		TL.get().leaveForId(query.getQueryString());
	}

	public static void clear() {
		TL.remove();
	}

	private static class HolderMap {

		private Map<String, GateKeepingCounter> holderMap = new HashMap<String, GateKeepingCounter>();

		public void enterForId(String id) throws JiraException {
			getOrCreateValueForId(id).enter();
		}

		private void leaveForId(String id) {
			getOrCreateValueForId(id).leave();
		}

		private GateKeepingCounter getOrCreateValueForId(String id) {
			if (!holderMap.containsKey(id)) {
				holderMap.put(id, new GateKeepingCounter());
			}
			return holderMap.get(id);
		}
	}

	private static class GateKeepingCounter {

		private final static int MAX_ENTERING_COUNT = 17;
		private int count = 0;

		public void enter() throws JiraException {
			verifyCanEnter();
			count++;
		}

		public void leave() {
			count--;
		}

		public void clear() {
			count = 0;
		}

		private void verifyCanEnter() throws JiraException {
			if (count >= MAX_ENTERING_COUNT) {
				clear();
				throw new JiraException("thread-local-gate.to-many-recurring-calls");
			}
		}
	}
}
