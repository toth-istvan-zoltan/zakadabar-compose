/*
 * @copyright@
 */
package zakadabar.template.frontend.browser.pages

import zakadabar.core.browser.page.ZkPage
import zakadabar.core.browser.util.plusAssign
import zakadabar.template.frontend.browser.resources.appStyles

object Home : ZkPage() {

    override fun onCreate() {
        classList += appStyles.home

        + "This is the home page of your application. Click on \"Login\" (on the left) and use \"so\" - \"so\" to log in."
    }

}