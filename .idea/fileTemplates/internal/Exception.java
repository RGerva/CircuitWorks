/**
 *  Exception: ${NAME}
 *  Custom exception for handling specific error scenarios.
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
public class ${NAME} extends RuntimeException {
  public ${NAME}(String message) {
    super(message);
  }
}
