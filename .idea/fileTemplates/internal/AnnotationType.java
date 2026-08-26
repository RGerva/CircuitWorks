/**
 *  Annotation Type: ${NAME}
 *  Defines metadata and behavior customization.
 *
 * <p>Created by: ${USER}
 * <p>On: ${YEAR}/${MONTH_NAME_SHORT}
 *
 * <p>GitHub: https://github.com/RGerva
 *
 * <p>Copyright (c) ${YEAR} @RGerva.
 *
 * <p>All Rights Reserved.
 */

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};

#end
#parse("File Header.java")
public @interface ${NAME} {
}
