/*
 * @copyright@
 */
package zakadabar.template.data

import kotlinx.serialization.Serializable
import zakadabar.core.data.EntityBo
import zakadabar.core.data.EntityBoCompanion
import zakadabar.core.data.EntityId
import zakadabar.core.schema.BoSchema


/**
 * Business Object of ExampleEntityBo.
 *
 * Generated with Bender at 2021-06-01T09:28:42.140Z.
 *
 * Please do not implement business logic in this class. If you add fields,
 * please check the frontend table and form, and also the persistence API on
 * the backend.
 */
@Serializable
class ExampleEntityBo(

    override var id: EntityId<ExampleEntityBo>,
    var name: String

) : EntityBo<ExampleEntityBo> {

    companion object : EntityBoCompanion<ExampleEntityBo>("example-entity")

    override fun getBoNamespace() = boNamespace
    override fun comm() = comm

    override fun schema() = BoSchema {
        + ::id
        + ::name min 1 max 100
    }

}