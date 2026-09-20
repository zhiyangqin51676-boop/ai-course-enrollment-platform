"""Lesson 3 · POST /can-graduate

Decide whether a student can graduate given their credits and GPA.

Business rule:
    credits >= 120  AND  gpa >= 2.0  ->  can_graduate = True
    otherwise                        ->  can_graduate = False

Fill in the TODOs below. Then wire this router into app/main.py:

    from app.routes import can_graduate
    app.include_router(can_graduate.router, tags=["lesson-3"])
"""
from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter()


# TODO 1: define GraduationRequest (Pydantic BaseModel) with two fields:
#         - credits: float
#         - gpa: float
#
#         AND a method `is_eligible(self) -> bool` that returns
#         True iff credits >= 120 AND gpa >= 2.0.
#
#         (This is the OOP part — data + behaviour packaged together.)
#
# class GraduationRequest(BaseModel):
#     ...


# TODO 2: define GraduationResponse (Pydantic BaseModel) with one field:
#         - can_graduate: bool
#
# class GraduationResponse(BaseModel):
#     ...


# TODO 3: define the endpoint. Signature:
#         @router.post("/can-graduate", response_model=GraduationResponse)
#         def check(req: GraduationRequest) -> GraduationResponse:
#             return GraduationResponse(can_graduate=req.is_eligible())
