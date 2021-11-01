/*
 * @copyright@
 */
package zakadabar.template.frontend.browser.resources

import zakadabar.core.browser.theme.ZkBuiltinLightTheme

class AppLightTheme : ZkBuiltinLightTheme() {

    companion object {
        const val NAME = "app-light"
    }

    override val name = NAME

    // -------------------------------------------------------------------------
    // Customize theme variables
    // -------------------------------------------------------------------------

//    override var primaryColor = "green"

    // -------------------------------------------------------------------------
    // Customize style variables
    // -------------------------------------------------------------------------

//    override fun onResume() {
//        super.onResume()
//
//        with(zkTitleBarStyles) {
//            appHandleBackground = "green"
//        }
//    }

}