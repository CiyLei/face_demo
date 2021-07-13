import json

import numpy as np
from flask import Flask, request, jsonify
import face_recognition

from result import Result

app = Flask(__name__)


# 识别图片中的人脸特征码和位置信息
@app.route('/detect', methods=['POST'])
def detect():
    try:
        if 'img' in request.files:
            imgFile = request.files['img']
            img = face_recognition.load_image_file(imgFile)
            # 获取人脸特征码
            encodes = face_recognition.face_encodings(img)
            encodes_result = []
            for encode in encodes:
                encodes_result.append(encode.tolist())
            # 获取人脸位置信息
            locations = face_recognition.face_locations(img)
            locations_result = []
            for location in locations:
                top, right, bottom, left = location
                locations_result.append({
                    'top': top,
                    'right': right,
                    'bottom': bottom,
                    'left': left,
                })
            # 返回特征码和位置信息
            return jsonify(Result({
                'encodes': encodes_result,
                'locations': locations_result,
            }).__dict__)
    except Exception as e:
        return jsonify(Result.error(1002, str(e)).__dict__)
    return jsonify(Result.error(1001, '未知错误').__dict__)


# 识别图片中的人脸和指定特征码的区别
@app.route('/distance', methods=['POST'])
def distance():
    try:
        if 'img' in request.files and 'encode' in request.form:
            imgFile = request.files['img']
            img = face_recognition.load_image_file(imgFile)
            # 获取传递图片的人脸特征码
            encodes = face_recognition.face_encodings(img)
            # 获取要比较的人脸特征码
            encode = json.loads(request.form.get('encode'))
            distances = face_recognition.face_distance(encodes, np.array(encode))
            locations_result = []
            # 是否需要人脸位置信息
            if 'location' in request.form and request.form.get('location') == '1':
                # 获取人脸位置信息
                locations = face_recognition.face_locations(img)
                for location in locations:
                    top, right, bottom, left = location
                    locations_result.append({
                        'top': top,
                        'right': right,
                        'bottom': bottom,
                        'left': left,
                    })
            return jsonify(Result({
                'distances': distances.tolist(),
                'locations': locations_result,
            }).__dict__)
    except Exception as e:
        return jsonify(Result.error(1002, str(e)).__dict__)
    return jsonify(Result.error(1001, '未知错误').__dict__)


if __name__ == '__main__':
    app.run('0.0.0.0', 8080)
