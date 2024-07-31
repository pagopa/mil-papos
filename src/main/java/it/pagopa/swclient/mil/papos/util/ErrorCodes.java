package it.pagopa.swclient.mil.papos.util;

import lombok.Getter;

@Getter
public final class ErrorCodes {
    private ErrorCodes() {
    }

    public static final String MODULE_ID = "00TR";

    /*
     * Validation errors code from 000001 to 000199
     */
    public static final String ERROR_REQUESTID_MUST_NOT_BE_NULL                                  = MODULE_ID + "000001";
    public static final String ERROR_AUTHORIZATION_MUST_NOT_BE_NULL                              = MODULE_ID + "000002";
    public static final String ERROR_PSPID_MUST_NOT_BE_NULL                                      = MODULE_ID + "000003";
    public static final String ERROR_BROKERID_MUST_NOT_BE_NULL                                   = MODULE_ID + "000004";
    public static final String ERROR_CHANNELID_MUST_NOT_BE_NULL                                  = MODULE_ID + "000005";
    public static final String ERROR_TERMINALHANDLERID_MUST_NOT_BE_NULL                          = MODULE_ID + "000006";
    public static final String ERROR_TERMINALID_MUST_NOT_BE_NULL                                 = MODULE_ID + "000007";
    public static final String ERROR_ENABLED_MUST_NOT_BE_NULL                                    = MODULE_ID + "000008";
    public static final String ERROR_PAYEECODE_MUST_NOT_BE_NULL                                  = MODULE_ID + "000009";
    public static final String ERROR_SLAVE_MUST_NOT_BE_NULL                                      = MODULE_ID + "000010";
    public static final String ERROR_PAGOPA_MUST_NOT_BE_NULL                                     = MODULE_ID + "000011";
    public static final String ERROR_IDPAY_MUST_NOT_BE_NULL                                      = MODULE_ID + "000012";
    public static final String ERROR_TERMINALDTO_MUST_NOT_BE_NULL                                = MODULE_ID + "000013";
    public static final String ERROR_WORKSTATIONS_MUST_NOT_BE_NULL                               = MODULE_ID + "000014";
    public static final String ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL                              = MODULE_ID + "000015";
    public static final String ERROR_NOTICE_NUMBER_MUST_NOT_BE_NULL                              = MODULE_ID + "000016";
    public static final String ERROR_AMOUNT_MUST_NOT_BE_NULL                                     = MODULE_ID + "000017";
    public static final String ERROR_STATUS_MUST_NOT_BE_NULL                                     = MODULE_ID + "000018";
    public static final String ERROR_SOLUTIONID_MUST_NOT_BE_NULL                                 = MODULE_ID + "000019";

    /*
     * Service errors code from 000200 to 000500
     */
    public static final String ERROR_GENERIC_FROM_DB                                             = MODULE_ID + "000200";
    public static final String ERROR_COUNTING_TERMINALS                                          = MODULE_ID + "000201";
    public static final String ERROR_LIST_TERMINALS                                              = MODULE_ID + "000202";
    public static final String ERROR_TERMINAL_NOT_FOUND                                          = MODULE_ID + "000203";
    public static final String ERROR_BULKLOADSTATUS_NOT_FOUND                                    = MODULE_ID + "000204";
    public static final String ERROR_PROCESSING_FILE                                             = MODULE_ID + "000205";
    public static final String ERROR_PARSING_DATE                                                = MODULE_ID + "000206";
    public static final String ERROR_LIST_TRANSACTIONS                                           = MODULE_ID + "000207";
    public static final String ERROR_COUNTING_TRANSACTIONS                                       = MODULE_ID + "000208";
    public static final String ERROR_TRANSACTION_NOT_FOUND                                       = MODULE_ID + "000209";
    public static final String ERROR_CHECK_TOKEN                                                 = MODULE_ID + "000210";
    public static final String ERROR_SOLUTION_NOT_FOUND                                          = MODULE_ID + "000211";
    public static final String ERROR_NO_SOLUTIONS_FOUND                                          = MODULE_ID + "000212";

    /*
     * Error descriptions
     */
    private static final String ERROR_REQUESTID_MUST_NOT_BE_NULL_DESCR = "RequestId must not be null";
    private static final String ERROR_AUTHORIZATION_MUST_NOT_BE_NULL_DESCR = "Authorization must not be null";
    private static final String ERROR_PSPID_MUST_NOT_BE_NULL_DESCR = "pspId must not be null";
    private static final String ERROR_BROKERID_MUST_NOT_BE_NULL_DESCR = "brokerId must not be null";
    private static final String ERROR_CHANNELID_MUST_NOT_BE_NULL_DESCR = "channelId must not be null";
    private static final String ERROR_TERMINALHANDLERID_MUST_NOT_BE_NULL_DESCR  = "terminalHandlerId must not be null";
    private static final String ERROR_TERMINALID_MUST_NOT_BE_NULL_DESCR = "terminalId must not be null";
    private static final String ERROR_ENABLED_MUST_NOT_BE_NULL_DESCR = "enabled must not be null";
    private static final String ERROR_PAYEECODE_MUST_NOT_BE_NULL_DESCR = "payeeCode must not be null";
    private static final String ERROR_SLAVE_MUST_NOT_BE_NULL_DESCR = "slave must not be null";
    private static final String ERROR_PAGOPA_MUST_NOT_BE_NULL_DESCR = "pagoPa must not be null";
    private static final String ERROR_IDPAY_MUST_NOT_BE_NULL_DESCR = "idpay must not be null";
    private static final String ERROR_DTO_MUST_NOT_BE_NULL_DESCR = "request body must not be null";
    private static final String ERROR_WORKSTATIONS_MUST_NOT_BE_NULL_DESCR = "workstations must not be null";
    private static final String ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL_DESCR = "file uploaded must not be null";
    private static final String ERROR_NOTICE_NUMBER_MUST_NOT_BE_NULL_DESCR = "noticeNumber must not be null";
    private static final String ERROR_AMOUNT_MUST_NOT_BE_NULL_DESCR = "amount must not be null";
    private static final String ERROR_STATUS_MUST_NOT_BE_NULL_DESCR = "status must not be null";
    private static final String ERROR_SOLUTIONID_MUST_NOT_BE_NULL_DESCR = "solutionId must not be null";

    private static final String ERROR_GENERIC_FROM_DB_DESCR = "unexpected error from db";
    private static final String ERROR_COUNTING_TERMINALS_DESCR = "error occurred while counting terminal";
    private static final String ERROR_LIST_TERMINALS_DESCR = "error occurred while retrieving list of paginated terminals";
    private static final String ERROR_TERMINAL_NOT_FOUND_DESCR = "terminal not found on db";
    private static final String ERROR_BULKLOADSTATUS_NOT_FOUND_DESCR = "bulkLoadStatus not found on db";
    private static final String ERROR_PROCESSING_FILE_DESCR = "error occurred during processing file";
    private static final String ERROR_PARSING_DATE_DESCR = "error occurred during parsing date";
    private static final String ERROR_LIST_TRANSACTIONS_DESCR = "error occurred while retrieving list of paginated transactions";
    private static final String ERROR_COUNTING_TRANSACTIONS_DESCR = "error occurred while counting transactions";
    private static final String ERROR_TRANSACTION_NOT_FOUND_DESCR = "transaction not found on db";
    private static final String ERROR_CHECK_TOKEN_DESCR = "check token fails, subject differs from pspId/payeeCode";
    private static final String ERROR_SOLUTION_NOT_FOUND_DESCR = "solution not found on db";
    private static final String ERROR_NO_SOLUTIONS_FOUND_DESCR = "no solutions found with given pspId and solutionIds";

    /*
     * Error complete message
     */
    public static final String ERROR_REQUESTID_MUST_NOT_BE_NULL_MSG = "[" + ERROR_REQUESTID_MUST_NOT_BE_NULL + "] " + ERROR_REQUESTID_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_AUTHORIZATION_MUST_NOT_BE_NULL_MSG = "[" + ERROR_AUTHORIZATION_MUST_NOT_BE_NULL + "] " + ERROR_AUTHORIZATION_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_PSPID_MUST_NOT_BE_NULL_MSG = "[" + ERROR_PSPID_MUST_NOT_BE_NULL + "] " + ERROR_PSPID_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_BROKERID_MUST_NOT_BE_NULL_MSG = "[" + ERROR_BROKERID_MUST_NOT_BE_NULL + "] " + ERROR_BROKERID_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_CHANNELID_MUST_NOT_BE_NULL_MSG = "[" + ERROR_CHANNELID_MUST_NOT_BE_NULL + "] " + ERROR_CHANNELID_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_TERMINALHANDLERID_MUST_NOT_BE_NULL_MSG = "[" + ERROR_TERMINALHANDLERID_MUST_NOT_BE_NULL + "] " + ERROR_TERMINALHANDLERID_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_TERMINALID_MUST_NOT_BE_NULL_MSG = "[" + ERROR_TERMINALID_MUST_NOT_BE_NULL + "] " + ERROR_TERMINALID_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_ENABLED_MUST_NOT_BE_NULL_MSG = "[" + ERROR_ENABLED_MUST_NOT_BE_NULL + "] " + ERROR_ENABLED_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_PAYEECODE_MUST_NOT_BE_NULL_MSG = "[" + ERROR_PAYEECODE_MUST_NOT_BE_NULL + "] " + ERROR_PAYEECODE_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_SLAVE_MUST_NOT_BE_NULL_MSG = "[" + ERROR_SLAVE_MUST_NOT_BE_NULL + "] " + ERROR_SLAVE_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_PAGOPA_MUST_NOT_BE_NULL_MSG = "[" + ERROR_PAGOPA_MUST_NOT_BE_NULL + "] " + ERROR_PAGOPA_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_IDPAY_MUST_NOT_BE_NULL_MSG = "[" + ERROR_IDPAY_MUST_NOT_BE_NULL + "] " + ERROR_IDPAY_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_DTO_MUST_NOT_BE_NULL_MSG = "[" + ERROR_TERMINALDTO_MUST_NOT_BE_NULL + "] " + ERROR_DTO_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_WORKSTATIONS_MUST_NOT_BE_NULL_MSG = "[" + ERROR_WORKSTATIONS_MUST_NOT_BE_NULL + "] " + ERROR_WORKSTATIONS_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL_MSG = "[" + ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL + "] " + ERROR_BULKLOAD_FILE_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_NOTICE_NUMBER_MUST_NOT_BE_NULL_MSG = "[" + ERROR_NOTICE_NUMBER_MUST_NOT_BE_NULL + "] " + ERROR_NOTICE_NUMBER_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_AMOUNT_MUST_NOT_BE_NULL_MSG = "[" + ERROR_AMOUNT_MUST_NOT_BE_NULL + "] " + ERROR_AMOUNT_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_STATUS_MUST_NOT_BE_NULL_MSG = "[" + ERROR_STATUS_MUST_NOT_BE_NULL + "] " + ERROR_STATUS_MUST_NOT_BE_NULL_DESCR;
    public static final String ERROR_SOLUTIONID_MUST_NOT_BE_NULL_MSG = "[" + ERROR_SOLUTIONID_MUST_NOT_BE_NULL + "] " + ERROR_SOLUTIONID_MUST_NOT_BE_NULL_DESCR;

    public static final String ERROR_GENERIC_FROM_DB_MSG = "[" + ERROR_GENERIC_FROM_DB + "] " + ERROR_GENERIC_FROM_DB_DESCR;
    public static final String ERROR_COUNTING_TERMINALS_MSG = "[" + ERROR_COUNTING_TERMINALS + "] " + ERROR_COUNTING_TERMINALS_DESCR;
    public static final String ERROR_LIST_TERMINALS_MSG = "[" + ERROR_LIST_TERMINALS + "] " + ERROR_LIST_TERMINALS_DESCR;
    public static final String ERROR_TERMINAL_NOT_FOUND_MSG = "[" + ERROR_TERMINAL_NOT_FOUND + "] " + ERROR_TERMINAL_NOT_FOUND_DESCR;
    public static final String ERROR_BULKLOADSTATUS_NOT_FOUND_MSG = "[" + ERROR_BULKLOADSTATUS_NOT_FOUND + "] " + ERROR_BULKLOADSTATUS_NOT_FOUND_DESCR;
    public static final String ERROR_PROCESSING_FILE_MSG = "[" + ERROR_PROCESSING_FILE + "] " + ERROR_PROCESSING_FILE_DESCR;
    public static final String ERROR_PARSING_DATE_MSG = "[" + ERROR_PARSING_DATE + "] " + ERROR_PARSING_DATE_DESCR;
    public static final String ERROR_LIST_TRANSACTIONS_MSG = "[" + ERROR_LIST_TRANSACTIONS + "] " + ERROR_LIST_TRANSACTIONS_DESCR;
    public static final String ERROR_COUNTING_TRANSACTIONS_MSG = "[" + ERROR_COUNTING_TRANSACTIONS + "] " + ERROR_COUNTING_TRANSACTIONS_DESCR;
    public static final String ERROR_TRANSACTION_NOT_FOUND_MSG = "[" + ERROR_TRANSACTION_NOT_FOUND + "] " + ERROR_TRANSACTION_NOT_FOUND_DESCR;
    public static final String ERROR_CHECK_TOKEN_MSG = "[" + ERROR_CHECK_TOKEN + "] " + ERROR_CHECK_TOKEN_DESCR;
    public static final String ERROR_SOLUTION_NOT_FOUND_MSG = "[" + ERROR_SOLUTION_NOT_FOUND + "] " + ERROR_SOLUTION_NOT_FOUND_DESCR;
    public static final String ERROR_NO_SOLUTIONS_FOUND_MSG = "[" + ERROR_NO_SOLUTIONS_FOUND + "] " + ERROR_NO_SOLUTIONS_FOUND_DESCR;

}
