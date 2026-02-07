package org.maiaframework.gen.spec.definition

object MaiaGenConstants {


    val FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO_CLASS_NAME = Fqcns.FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO.uqcn.value


    const val FOREIGN_KEY_REFERENCE_SERVICE_CLASS_NAME = "ForeignKeyReferenceService"


    const val FOREIGN_KEY_REFERENCE_SERVICE_IMPORT_STATEMENT = "import { $FOREIGN_KEY_REFERENCE_SERVICE_CLASS_NAME } from '@app/gen-components/common/services/foreign-key-reference.service';"


    const val FOREIGN_KEY_REFERENCE_SERVICE_RENDERED_FILE_PATH = "/app/gen-components/common/services/foreign-key-reference.service.ts"


    const val FOREIGN_KEY_REFERENCES_EXIST_RESPONSE_DTO_RENDERED_FILE_PATH = "/app/gen-components/common/model/ForeignKeyReferencesExistResponseDto.ts"


    const val FORM_VALIDATION_RESPONSE_DTO_RENDERED_FILE_PATH = "/app/gen-components/common/model/FormValidationResponseDto.ts"


    const val TOTAL_HITS_RELATION_RENDERED_FILE_PATH = "/app/gen-components/common/model/TotalHitsRelation.ts"


}
