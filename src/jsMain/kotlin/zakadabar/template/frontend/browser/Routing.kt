/*
 * Copyright © 2020, Simplexion, Hungary and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package zakadabar.template.frontend.browser

import zakadabar.core.browser.application.ZkAppRouting
import zakadabar.template.frontend.browser.pages.ExampleEntityCrud
import zakadabar.template.frontend.browser.pages.Home

class Routing : ZkAppRouting(DefaultLayout, Home) {

    init {
        zakadabar.lib.accounts.browser.install(this)
        zakadabar.lib.i18n.browser.install(this)
        + Home
        + ExampleEntityCrud()
   }

}