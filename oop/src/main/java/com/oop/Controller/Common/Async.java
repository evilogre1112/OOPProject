package com.oop.Controller.Common;

import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.swing.SwingWorker;

import com.oop.EXCEPTION.PermissionException;

/** Chạy việc nặng (DB) ở luồng nền bằng SwingWorker, kết quả trả về trên EDT. */
public final class Async {
    private Async() {
    }

    /** Chạy task nền; xong thì gọi onSuccess trên EDT, lỗi thì hiện hộp thoại. */
    public static <T> void run(Component owner, Callable<T> task, Consumer<T> onSuccess) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                T result;
                try {
                    result = get();
                } catch (Exception e) {
                    showError(owner, e);
                    return;
                }
                onSuccess.accept(result);
            }
        }.execute();
    }

    /** Dành cho thao tác thêm/sửa/xóa trả về boolean: true thì chạy onSuccess, false thì báo thất bại. */
    public static void action(Component owner, Callable<Boolean> task, Runnable onSuccess) {
        run(owner, task, ok -> {
            if (Boolean.TRUE.equals(ok)) {
                onSuccess.run();
            } else {
                Dialogs.error(owner, "Thao tác không thành công, hãy kiểm tra lại dữ liệu.");
            }
        });
    }

    private static void showError(Component owner, Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        String message;
        if (cause instanceof PermissionException) {
            message = "Bạn không có quyền thực hiện thao tác này";
        } else if (cause.getMessage() != null) {
            message = cause.getMessage();
        } else {
            message = cause.getClass().getSimpleName();
        }
        Dialogs.error(owner, message);
    }
}
