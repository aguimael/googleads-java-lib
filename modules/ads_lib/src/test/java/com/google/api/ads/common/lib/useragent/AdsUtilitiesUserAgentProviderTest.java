// Copyright 2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.api.ads.common.lib.useragent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import com.google.api.ads.common.lib.conf.AdsLibConfiguration;
import com.google.api.ads.common.lib.utils.AdsUtility;
import com.google.api.ads.common.lib.utils.AdsUtilityRegistry;
import com.google.api.ads.common.lib.utils.Internals;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

/**
 * Test for {@link AdsUtilitiesUserAgentProvider}.
 */
@RunWith(JUnit4.class)
public class AdsUtilitiesUserAgentProviderTest {
  private AdsUtilitiesUserAgentProvider userAgentProvider;
  private AdsUtilityRegistry adsUtilityRegistry;

  @Mock
  private Provider<Internals> internalsProvider;

  @Mock
  private Internals internals;

  @Mock
  private AdsLibConfiguration adsLibConfiguration;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    adsUtilityRegistry = new AdsUtilityRegistry();
    when(internalsProvider.get()).thenReturn(internals);
    when(internals.getAdsUtilityRegistry()).thenReturn(adsUtilityRegistry);
    userAgentProvider = new AdsUtilitiesUserAgentProvider(internalsProvider, adsLibConfiguration);
  }

  @After
  public void tearDown() throws Exception {
    assertEquals("User agent provider should clear utilities", Sets.newHashSet(),
        internals.getAdsUtilityRegistry().getRegisteredUtilities());
  }

  /**
   * Tests that a single registered utility will result in the correct user agent if include
   * utilities is true.
   */
  @Test
  public void testGenerateUserAgent_utilityRegistered_includeUtilitiesTrue() {
    when(adsLibConfiguration.isIncludeAdsUtilitiesInUserAgent()).thenReturn(true);

    internals.getAdsUtilityRegistry().addUtility(AdsUtility.PRODUCT_PARTITION_TREE);

    String actualUserAgent = userAgentProvider.getUserAgent();
    assertEquals("User agent should include registered utilities if include utilities is true",
        AdsUtility.PRODUCT_PARTITION_TREE.getUserAgentIdentifier(), actualUserAgent);
  }

  /**
   * Tests that the user agent provider will always return multiple user agent identifiers
   * in alphabetical order, regardless of the order of registration.
   */
  @Test
  public void testGenerateUserAgent_multipleUtilitiesRegistered_includeUtilitiesTrue() {
    when(adsLibConfiguration.isIncludeAdsUtilitiesInUserAgent()).thenReturn(true);

    List<AdsUtility> adsUtilities =
        Lists.newArrayList(
            AdsUtility.PRODUCT_PARTITION_TREE,
            AdsUtility.SELECTOR_BUILDER,
            AdsUtility.REPORT_DOWNLOADER);

    for (int i = 0; i < adsUtilities.size() * adsUtilities.size(); i++) {
      for (AdsUtility adsUtility : adsUtilities) {
        internals.getAdsUtilityRegistry().addUtility(adsUtility);
      }

      String expectedUserAgent =
          "ProductPartitionTree, ReportDownloader, SelectorBuilder";
      String actualUserAgent = userAgentProvider.getUserAgent();
      assertEquals(
          "User agent should include registered utilities in alphabetical order if include "
          + "utilities is true",
          expectedUserAgent, actualUserAgent);
      assertEquals("User agent provider should clear utilities", Sets.newHashSet(),
          internals.getAdsUtilityRegistry().getRegisteredUtilities());

      // Rotate the list of utilities in preparation for the next iteration.
      Collections.rotate(adsUtilities, i);
    }
  }

  /**
   * Tests that if no utilities are registered, the provider will return null.
   */
  @Test
  public void testGenerateUserAgent_noUtitilitiesRegistered_includeUtilitiesTrue() {
    when(adsLibConfiguration.isIncludeAdsUtilitiesInUserAgent()).thenReturn(true);

    String actualUserAgent = userAgentProvider.getUserAgent();

    assertNull("User agent should be null if no utilities are registered", actualUserAgent);
  }

  /**
   * Tests that if include utilities is false, the provider will return null (even if utilities
   * are registered).
   */
  @Test
  public void testGenerateUserAgent_utilitiesRegistered_includeUtilitiesFalse() {
    when(adsLibConfiguration.isIncludeAdsUtilitiesInUserAgent()).thenReturn(false);

    internals.getAdsUtilityRegistry().addUtility(AdsUtility.PRODUCT_PARTITION_TREE);

    String actualUserAgent = userAgentProvider.getUserAgent();
    assertNull("User agent should be null if include utilities is false", actualUserAgent);
  }
}
