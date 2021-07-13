class Result:
    def __init__(self, data, code=200, msg=''):
        self.data = data
        self.code = code
        self.msg = msg

    @staticmethod
    def error(code, msg=''):
        return Result(None, code, msg)
