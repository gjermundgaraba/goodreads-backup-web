(function () {
    $(document).ajaxStop($.unblockUI);
    var backupButton = $("#backup-button");
    var downloadButton = $('#download-button');
    var userIdField = $("#user-id");
    backupButton.click(performBackup);

    function performBackup() {
        var userId = userIdField.val();
        if (!userId) {
            createErrorToast('You need to provide a user id');
        } else if (!isNumeric(userId)) {
            createErrorToast('User id needs to be a number');
        } else {
            $.blockUI({
                message: '<h1>Getting your backup file ready for download. This can take a few minutes.</h1>'
            });
            var backupUrl = window.location +  '/backup?userId=' + userId;
            $.get(backupUrl, makeDownloadButtonAvailable)
                .fail(handleAjaxError);
        }
    }

    function makeDownloadButtonAvailable(backupId) {
        if (backupId) {
            downloadButton.click(function (event) {
                event.preventDefault();  //stop the browser from following
                window.location.href = window.location + '/backup/' + backupId + '/download';

                userIdField.val('');
                backupButton.show();
                userIdField.show();
                downloadButton.hide();
            });

            backupButton.hide();
            userIdField.hide();
            downloadButton.show();
        }
    }

    function handleAjaxError(error) {
        var errorMessage = "Something went wrong!";
        if (error && error.responseJSON && error.responseJSON.message) {
            errorMessage = error.responseJSON.message;
        }
        createErrorToast(errorMessage, 'Backup Failed');
    }

    function isNumeric(num){
        return !isNaN(num)
    }

    function createErrorToast(errorMessage, errorHeading) {
        var heading = errorHeading ? errorHeading : 'Error';
        $.toast({
            heading: heading,
            text: errorMessage,
            position: 'mid-center',
            hideAfter: false,
            icon: 'error'
        })
    }

})();
