/*
 * @copyright@
 */
package zakadabar.template.backend.persistence

import zakadabar.core.persistence.exposed.ExposedPaTable
import zakadabar.template.data.ExampleEntityBo

object ExampleEntityTable : ExposedPaTable<ExampleEntityBo>(
    tableName = "example_entity"
) {

    internal val name = varchar("name", 100)

}