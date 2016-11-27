(function () {
    $(document).ajaxStop($.unblockUI);
    var backupButton = $("#backup-button");
    var downloadButton = $('#download-button');
    var userIdField = $("#user-id");
    backupButton.click(performBackup);

    function performBackup() {
        var userId = userIdField.val();
        // TODO: Handle when no userId is added

        $.blockUI({
            message: '<h1>Getting your backup file ready for download. This can take a few minutes.</h1>'
        });
        var backupUrl = window.location +  '/backup?userId=' + userId;
        $.get(backupUrl, makeDownloadButtonAvailable);
        // TODO: Handle error from get
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

})();
