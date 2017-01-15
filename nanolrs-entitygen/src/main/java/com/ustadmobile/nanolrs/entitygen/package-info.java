/**
 * Entity Gen generates ORM Entity classes from the model interfaces in nanolrs-core project
 * (see com.ustadmobile.nanolrs.core.model package).  Each interface has only getter/setter methods
 * in the form of get/is/set .
 *
 * The primary key id property should be named from one of the values in EntityGenerator.PRIMARY_KEY_PROPERTY_NAMES
 * and there must not be more than one property containing of those names in the interface.
 *
 * The following javadoc tags are supported to control generation of entity classes:
 *
 * \@nanolrs.datatype : Constant as per EntityGenerator.DATA_TYPE_ strings
 *
 * See the individual class (EntityGeneratorOrmLite or EntityGeneratorSharkOrm) for details of
 * the code generation output for that ORM.
 *
 */
package com.ustadmobile.nanolrs.entitygen;