/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr-nci-uk/LICENSE.txt for details.
 */

package org.exist.fulltext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        FTIndexTest.class,
        FtQueryTest.class,
        FTMatchListenerTest.class
})
public class AllFulltextTests {
}
